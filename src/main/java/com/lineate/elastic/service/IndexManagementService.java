package com.lineate.elastic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineate.elastic.api.doc.ElasticDocApi;
import com.lineate.elastic.api.index.ElasticIndexApi;
import com.lineate.elastic.api.task.ElasticTaskApi;
import com.lineate.elastic.configuration.EntitySearchProperties;
import com.lineate.elastic.dto.StatusResponse;
import com.lineate.elastic.dto.TaskStatusResponse;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IndexManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexManagementService.class);
    private final ConcurrentHashMap<String, String> elasticTasks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ElasticIndexApi indexApi;
    private final ElasticDocApi docApi;
    private final ElasticTaskApi taskApi;


    public IndexManagementService(ObjectMapper objectMapper, ElasticIndexApi indexApi, ElasticDocApi docApi, ElasticTaskApi taskApi) {
        this.objectMapper = objectMapper;
        this.indexApi = indexApi;
        this.docApi = docApi;
        this.taskApi = taskApi;
    }

    public StatusResponse createIndex(EntitySearchProperties properties) {
        LOGGER.info("Creating index with name {} using config file {}",
                properties.getIndexName(), properties.getConfigFile());
        if (indexApi.checkIndexExists(properties.getIndexName())) {
            return new StatusResponse(StatusResponse.Status.ERROR, "Index already exists");
        }
        String newIndexName = properties.getIndexName() + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        if (indexApi.createIndex(newIndexName, properties.getConfigFile())
                && indexApi.addAliasToIndex(newIndexName, properties.getIndexName())) {
            return StatusResponse.OK;
        }
        return new StatusResponse(StatusResponse.Status.ERROR, "Could not create index");
    }

    public StatusResponse deleteIndex(EntitySearchProperties properties) {
        LOGGER.info("Deleting index {}", properties.getIndexName());

        String indexRealName = indexApi.getIndexNameByAlias(properties.getIndexName());
        if (indexRealName != null) {
            if (indexApi.deleteIndex(indexRealName)) {
                return StatusResponse.OK;
            }
        }
        return new StatusResponse(StatusResponse.Status.ERROR, "Could not delete index");
    }

    public StatusResponse reindex(EntitySearchProperties properties) {
        LOGGER.info("Starting reindex for {}", properties.getIndexName());

        if (!indexApi.checkIndexExists(properties.getIndexName())) {
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "Could not find index.");
        }

        String newIndexName = properties.getIndexName() + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        if (!indexApi.createIndex(newIndexName, properties.getConfigFile())) {
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "Could not create empty index to reindex documents into.");
        }

        String taskId = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (taskId != null) {
            GetTaskResponse taskInfoResponse = taskApi.getTaskInfo(taskId, false);
            if (!taskInfoResponse.isCompleted()) {
                return new StatusResponse(StatusResponse.Status.ERROR,
                        "Reindexing task is already running for the index.");
            }
            elasticTasks.remove(properties.getIndexName());
        }

        taskId = docApi.submitReindexTask(properties.getIndexName(), newIndexName);
        if (taskId == null) {
            indexApi.deleteIndex(newIndexName);
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "Could not submit reindexing task");
        }

        elasticTasks.put(properties.getIndexName(), taskId);
        return StatusResponse.OK;
    }

    public StatusResponse cancelReindexing(EntitySearchProperties properties) {
        String taskId = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (taskId == null) {
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "There are no tasks running for the index");
        }
        if (taskApi.cancelTask(taskId)) {
            return StatusResponse.OK;
        }
        return new StatusResponse(StatusResponse.Status.ERROR,
                "Could not cancel task");
    }

    public TaskStatusResponse getReindexingTaskStatus(EntitySearchProperties properties) {

        String taskId = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (taskId == null) {
            return new TaskStatusResponse();
        }

        GetTaskResponse taskInfoResponse = taskApi.getTaskInfo(taskId, false);
        TaskStatusResponse taskStatusResponse = new TaskStatusResponse();
        taskStatusResponse.setCompleted(taskInfoResponse.isCompleted());
        taskStatusResponse.setSpentTimeMs(taskInfoResponse.getTaskInfo().getRunningTimeNanos() / 1000000);
        try {
            JsonNode statusJson;
            statusJson = objectMapper.readTree(taskInfoResponse.getTaskInfo().getStatus().toString());
            taskStatusResponse.setTotal(statusJson.get("total").asInt());
            taskStatusResponse.setUpdated(statusJson.get("updated").asInt());
            taskStatusResponse.setCreated(statusJson.get("created").asInt());
            taskStatusResponse.setDeleted(statusJson.get("deleted").asInt());
            JsonNode canceled = statusJson.get("canceled");
            if (canceled != null) {
                taskStatusResponse.setCanceled(canceled.asText());
            }
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error occurred while parsing task status", e);
        }

        return taskStatusResponse;
    }
}
