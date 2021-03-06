package com.lineate.elastic.api;

import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.CancelTasksRequest;
import org.elasticsearch.client.tasks.CancelTasksResponse;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.tasks.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ElasticTaskApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticTaskApi.class);
    private final RestHighLevelClient client;

    public ElasticTaskApi(RestHighLevelClient client) {
        this.client = client;
    }

    public GetTaskResponse getTaskInfo(final String taskIdString, final boolean waitForCompletion) {
        try {
            LOGGER.info("Loading info for task with id {}", taskIdString);
            String[] taskId = taskIdString.split(":");
            GetTaskRequest request = new GetTaskRequest(taskId[0], Long.parseLong(taskId[1]));
            request.setWaitForCompletion(waitForCompletion);

            GetTaskResponse response = client.tasks().get(request, RequestOptions.DEFAULT).orElse(null);
            if (response != null) {
                TaskInfo taskInfo = response.getTaskInfo();
                LOGGER.info("Successfully retrieved info for task. Completed {}, Status {}", response.isCompleted(), taskInfo.toString());
                return response;
            }
            LOGGER.info("Could not retrieve info for task {}", taskIdString);
            throw new ElasticActionFailedException("Could not retrieve info for task.");

        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while retrieving info for a task.", e);
            throw new ElasticActionFailedException("Error occurred while retrieving info for a task", e);
        }
    }

    public void cancelTask(String taskIdString) {
        try {
            LOGGER.info("Cancelling task with id: {}", taskIdString);
            CancelTasksRequest request = new CancelTasksRequest.Builder()
                    .withTaskId(new org.elasticsearch.client.tasks.TaskId(taskIdString))
                    .withWaitForCompletion(false)
                    .build();
            CancelTasksResponse response = client.tasks().cancel(request, RequestOptions.DEFAULT);
            List<org.elasticsearch.client.tasks.TaskInfo> taskInfoList = response.getTasks();
            if (!taskInfoList.isEmpty()) {
                LOGGER.info("Successfully cancelled task with id {}", taskIdString);
            } else {
                LOGGER.info("No tasks were cancelled");
            }
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while canceling a task.", e);
            throw new ElasticActionFailedException("Error occurred while canceling a task.", e);
        }
    }
}
