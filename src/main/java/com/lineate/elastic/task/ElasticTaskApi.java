package com.lineate.elastic.task;

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

    public TaskInfo getTaskInfo(final String taskIdString, final boolean waitForCompletion) throws IOException {
        try {
            LOGGER.info("Loading info for task with id {}", taskIdString);
            String[] taskId = taskIdString.split(":");
            GetTaskRequest request = new GetTaskRequest(taskId[0], Long.parseLong(taskId[1]));
            request.setWaitForCompletion(waitForCompletion);

            GetTaskResponse response = client.tasks().get(request, RequestOptions.DEFAULT).orElse(null);
            if (response != null) {
                TaskInfo taskInfo = response.getTaskInfo();
                LOGGER.info("Successfully retrieved info for task. Completed {}, Status {}", response.isCompleted(), taskInfo.toString());
                return taskInfo;
            } else {
                LOGGER.info("Could not retrieve info for task {}", taskIdString);
                return null;
            }
        } catch (IOException e) {
            LOGGER.warn("Error occurred while retrieving info for a task", e);
            throw e;
        }
    }

    public boolean cancelTask(String taskIdString) throws IOException {
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
                return true;
            } else {
                LOGGER.info("No tasks were cancelled");
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn("Error occurred while canceling a task", e);
            throw e;
        }
    }
}
