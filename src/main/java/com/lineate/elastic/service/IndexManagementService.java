package com.lineate.elastic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineate.elastic.api.ElasticDocApi;
import com.lineate.elastic.api.ElasticIndexApi;
import com.lineate.elastic.api.ElasticTaskApi;
import com.lineate.elastic.configuration.EntitySearchProperties;
import com.lineate.elastic.dto.StatusResponse;
import com.lineate.elastic.dto.TaskStatusResponse;
import com.lineate.elastic.exception.ElasticActionFailedException;
import com.lineate.elastic.exception.ElasticActionForbiddenException;
import com.lineate.elastic.exception.ElasticEntityNotFoundException;
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
            throw new ElasticActionForbiddenException("Index already exists");
        }
        String newIndexName = properties.getIndexName() + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        indexApi.createIndex(newIndexName, properties.getConfigFile());
        indexApi.addAliasToIndex(newIndexName, properties.getIndexName());
        return StatusResponse.OK;
    }

    public StatusResponse deleteIndex(EntitySearchProperties properties) {
        LOGGER.info("Deleting index {}", properties.getIndexName());
        String indexRealName = indexApi.getIndexNameByAlias(properties.getIndexName());
        indexApi.deleteIndex(indexRealName);
        return StatusResponse.OK;
    }

    public StatusResponse reindex(EntitySearchProperties properties) {
        LOGGER.info("Starting reindexing for {}", properties.getIndexName());

        if (!indexApi.checkIndexExists(properties.getIndexName())) {
            throw new ElasticEntityNotFoundException("Could not find index");
        }

        TrackedReindexingTask trackedReindexingTask = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (trackedReindexingTask != null && trackedReindexingTask.isTracking()) {
            throw new ElasticActionForbiddenException("Reindexing task is already running for the index.");
        }

        String newIndexName = properties.getIndexName() + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        indexApi.createIndex(newIndexName, properties.getConfigFile());
        String taskId;
        try {
            taskId = docApi.submitReindexTask(properties.getIndexName(), newIndexName);
        } catch (ElasticActionFailedException ex) {
            indexApi.deleteIndex(newIndexName);
            throw ex;
        }

        String oldIndexName = indexApi.getIndexNameByAlias(properties.getIndexName());
        trackedReindexingTask = new TrackedReindexingTask(oldIndexName, newIndexName, properties.getIndexName(), taskId);
        elasticTasks.put(properties.getIndexName(), trackedReindexingTask);
        return StatusResponse.OK;
    }

    public StatusResponse cancelReindexing(EntitySearchProperties properties) {
        LOGGER.info("Canceling task for reindexing {}", properties.getIndexName());

        TrackedReindexingTask trackedReindexingTask = elasticTasks.getOrDefault(properties.getIndexName(), null);
        if (trackedReindexingTask == null || !trackedReindexingTask.isTracking()) {
            throw new ElasticEntityNotFoundException("There are no tasks running for the index");
        }

        String taskId = trackedReindexingTask.getElasticTaskId();
        taskApi.cancelTask(taskId);
        return StatusResponse.OK;
    }

    public TaskStatusResponse getReindexingTaskStatus(EntitySearchProperties properties) {
        LOGGER.info("Retrieving reindexing task status for {}", properties.getIndexName());

        TrackedReindexingTask trackedReindexingTask = elasticTasks.getOrDefault(properties.getIndexName(), null);

        if (trackedReindexingTask == null) {
            throw new ElasticEntityNotFoundException("Elastic search task not found");
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

    @Scheduled(fixedRateString = "${search.trackingTaskRequestInterval}")
    private void manageReindexingTask() {
        elasticTasks.values()
                .stream()
                .filter(TrackedReindexingTask::isTracking)
                .forEach(trackedReindexingTask -> {
                    try {
                        String taskId = trackedReindexingTask.getElasticTaskId();
                        GetTaskResponse getTaskResponse;
                        getTaskResponse = taskApi.getTaskInfo(taskId, false);

                        if (getTaskResponse.isCompleted()) {

                            JsonNode statusJson = objectMapper.readTree(getTaskResponse.getTaskInfo().getStatus().toString());
                            JsonNode canceled = statusJson.get("canceled");

                            if (canceled == null) {
                                LOGGER.info("Reindexing completed for task {}", trackedReindexingTask.getElasticTaskId());

                                LOGGER.info("Deleting old index {}", trackedReindexingTask.getSrcIndexName());
                                indexApi.deleteIndex(trackedReindexingTask.getSrcIndexName());

                                LOGGER.info("Adding alias {} to index {}.",
                                        trackedReindexingTask.getIndexAlias(),
                                        trackedReindexingTask.getDstIndexName());

                                indexApi.addAliasToIndex(trackedReindexingTask.getDstIndexName(),
                                        trackedReindexingTask.getIndexAlias());
                            } else {
                                LOGGER.info("Reindexing task {} was canceled. Deleting newly created index {}.",
                                        trackedReindexingTask.getElasticTaskId(),
                                        trackedReindexingTask.getDstIndexName());
                                try {
                                    indexApi.deleteIndex(trackedReindexingTask.getDstIndexName());
                                } catch (ElasticActionFailedException e) {
                                    LOGGER.warn("Could not clean up after reindexing cancellation.");
                                }
                            }
                            trackedReindexingTask.setTracking(false);
                        }
                    } catch (ElasticActionFailedException e) {
                        LOGGER.warn("Error occurred while tracking task {}", trackedReindexingTask.getElasticTaskId());
                    } catch (JsonProcessingException e) {
                        LOGGER.warn("Parsing error occurred while tracking task {}",
                                trackedReindexingTask.getElasticTaskId(), e);
                    }
                });
    }
}
