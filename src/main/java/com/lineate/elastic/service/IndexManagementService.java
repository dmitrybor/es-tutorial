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
import com.lineate.elastic.model.TrackedReindexingTask;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class IndexManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexManagementService.class);

    private final ConcurrentMap<String, TrackedReindexingTask> elasticTasks = new ConcurrentHashMap<>();
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

        TrackedReindexingTask trackedReindexingTask = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (trackedReindexingTask != null && trackedReindexingTask.isTracking()) {
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "Reindexing task is already running for the index.");
        }

        String newIndexName = properties.getIndexName() + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        if (!indexApi.createIndex(newIndexName, properties.getConfigFile())) {
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "Could not create empty index to reindex documents into.");
        }

        String taskId = docApi.submitReindexTask(properties.getIndexName(), newIndexName);
        if (taskId == null) {
            indexApi.deleteIndex(newIndexName);
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "Could not submit reindexing task");
        }

        String oldIndexName = indexApi.getIndexNameByAlias(properties.getIndexName());
        trackedReindexingTask = new TrackedReindexingTask(oldIndexName, newIndexName, properties.getIndexName(), taskId);
        elasticTasks.put(properties.getIndexName(), trackedReindexingTask);
        return StatusResponse.OK;
    }

    public StatusResponse cancelReindexing(EntitySearchProperties properties) {

        TrackedReindexingTask trackedReindexingTask = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (trackedReindexingTask == null || !trackedReindexingTask.isTracking()) {
            return new StatusResponse(StatusResponse.Status.ERROR,
                    "There are no tasks running for the index");
        }

        String taskId = trackedReindexingTask.getElasticTaskId();
        if (taskApi.cancelTask(taskId)) {
            return StatusResponse.OK;
        }
        return new StatusResponse(StatusResponse.Status.ERROR,
                "Could not cancel task");
    }

    public TaskStatusResponse getReindexingTaskStatus(EntitySearchProperties properties) {

        TrackedReindexingTask trackedReindexingTask = elasticTasks.getOrDefault(properties.getIndexName(), null);

        if (trackedReindexingTask == null) {
            return new TaskStatusResponse();
        }

        String taskId = trackedReindexingTask.getElasticTaskId();
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

    @Scheduled(fixedRate = 10000)
    private void manageReindexingTask() {
        elasticTasks.values()
                .stream()
                .filter(TrackedReindexingTask::isTracking)
                .forEach(trackedReindexingTask -> {
                    String taskId = trackedReindexingTask.getElasticTaskId();
                    GetTaskResponse getTaskResponse = taskApi.getTaskInfo(taskId, false);
                    if (getTaskResponse == null) {
                        LOGGER.warn("Could not get response while tracking task {}",
                                trackedReindexingTask.getElasticTaskId());
                        return;
                    }
                    if (getTaskResponse.isCompleted()) {
                        JsonNode canceled;
                        try {
                            JsonNode statusJson;
                            statusJson = objectMapper.readTree(getTaskResponse.getTaskInfo().getStatus().toString());
                            canceled = statusJson.get("canceled");
                        } catch (JsonProcessingException e) {
                            LOGGER.warn("Error occurred while parsing task status", e);
                            return;
                        }
                        if (canceled == null) {
                            LOGGER.info("Reindexing completed for task {}", trackedReindexingTask.getElasticTaskId());

                            LOGGER.info("Deleting old index {}", trackedReindexingTask.getSrcIndexName());
                            indexApi.deleteIndex(trackedReindexingTask.getSrcIndexName());

                            LOGGER.info("Adding alias {} to index {}.",
                                    trackedReindexingTask.getIndexAlias(),
                                    trackedReindexingTask.getDstIndexName());

                            if (!indexApi.addAliasToIndex(trackedReindexingTask.getDstIndexName(),
                                    trackedReindexingTask.getIndexAlias())) {
                                LOGGER.warn("Could not add alias {} to {}. Will try again later.",
                                        trackedReindexingTask.getIndexAlias(),
                                        trackedReindexingTask.getDstIndexName());
                                return;
                            }
                        } else {
                            LOGGER.info("Reindexing task {} was canceled. Deleting newly created index {}.",
                                    trackedReindexingTask.getElasticTaskId(),
                                    trackedReindexingTask.getDstIndexName());
                            indexApi.deleteIndex(trackedReindexingTask.getDstIndexName());
                        }

                        trackedReindexingTask.setTracking(false);
                    }
                });
    }
}
