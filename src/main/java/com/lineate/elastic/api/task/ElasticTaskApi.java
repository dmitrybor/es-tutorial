package com.lineate.elastic.api.task;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.tasks.TaskId;
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
            } else {
                LOGGER.info("Could not retrieve info for task {}", taskIdString);
                return null;
            }
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while retrieving info for a task", e);
            return null;
        }
    }

    public boolean cancelTask(String taskIdString) {
        try {
            LOGGER.info("Cancelling task with id: {}", taskIdString);
            CancelTasksRequest request = new CancelTasksRequest();
            request.setTaskId(new TaskId(taskIdString));
            CancelTasksResponse response = client.tasks().cancel(request, RequestOptions.DEFAULT);
            List<TaskInfo> taskInfoList = response.getTasks();
            if (!taskInfoList.isEmpty()) {
                LOGGER.info("Successfully cancelled task with id {}", taskIdString);
                return true;
            } else {
                LOGGER.info("No tasks were cancelled");
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn("Error occurred while canceling a task", e);
            return false;
        }
    }
}
