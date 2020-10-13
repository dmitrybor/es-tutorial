package com.lineate.elastic.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lineate.elastic.configuration.SearchProperties;
import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ElasticDocApi {
    private static Logger LOGGER = LoggerFactory.getLogger(ElasticIndexApi.class);
    private final RestHighLevelClient client;
    private final SearchProperties searchProperties;

    public ElasticDocApi(RestHighLevelClient client, SearchProperties searchProperties) {
        this.client = client;
        this.searchProperties = searchProperties;
    }

    public void reindex(final String fromIndex, final String toIndex) {
        try {
            LOGGER.info("Reindexing documents from {} to {}", fromIndex, toIndex);

            ReindexRequest request = prepareReindexRequest(fromIndex, toIndex);
            request.setTimeout(TimeValue.timeValueMinutes(60));

            BulkByScrollResponse bulkResponse =
                    client.reindex(request, RequestOptions.DEFAULT);

            if (bulkResponse.isTimedOut()) {
                LOGGER.info("Could not reindex documents from {} to {}", fromIndex, toIndex);
                throw new ElasticActionFailedException("Could not reindex documents.");
            }
            LOGGER.info("Successfully reindexed {} documents from {} to {} in {} ms",
                    bulkResponse.getTotal(), fromIndex, toIndex, bulkResponse.getTook().millis());


        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while reindexing", e);
            throw new ElasticActionFailedException("Error occurred while reindexing.", e);
        }
    }

    public String submitReindexTask(final String fromIndex, final String toIndex) {
        try {
            LOGGER.info("Submitting a task to reindex documents from {} to {}", fromIndex, toIndex);
            ReindexRequest request = prepareReindexRequest(fromIndex, toIndex);
            TaskSubmissionResponse response = client.submitReindexTask(request, RequestOptions.DEFAULT);
            String taskId = response.getTask();
            LOGGER.info("Reindexing task successfully submitted, task id: {}", taskId);
            return taskId;
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while submitting reindexing task.", e);
            throw new ElasticActionFailedException("Error occurred while submitting reindexing task.", e);
        }
    }

    public boolean bulkIndexFromNdJsonFile(final String indexName, final String ndJsonFileName) throws IOException {
        try {
            LOGGER.info("Indexing documents into {} from file {}", indexName, ndJsonFileName);
            BulkRequest bulkRequest;
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(ndJsonFileName);
                 InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, Charset.defaultCharset());
                 BufferedReader reader = new BufferedReader(inputStreamReader)
            ) {
                if (resourceAsStream == null) {
                    LOGGER.warn("Cannot index content json file file {}", ndJsonFileName);
                    return false;
                }

                bulkRequest = new BulkRequest();
                ObjectMapper objectMapper = new ObjectMapper();
                String objectString;
                while ((objectString = reader.readLine()) != null) {
                    JsonNode docIdJson = objectMapper.readTree(objectString);
                    ObjectNode indexNode = (ObjectNode) docIdJson.get("index");
                    String documentId = indexNode.get("_id").asText();
                    objectString = reader.readLine();

                    IndexRequest request = new IndexRequest(indexName)
                            .id(documentId)
                            .source(objectString, XContentType.JSON);
                    bulkRequest.add(request);
                }
            }

            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.status() == RestStatus.OK) {
                LOGGER.info("{} Documents were successfully indexed from file in {} ms",
                        bulkResponse.getItems().length,
                        bulkResponse.getTook().millis());
                return true;
            } else {
                LOGGER.info("Could not index documents form file");
                return false;
            }

        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error during bulk indexing", e);
            throw e;
        }
    }

    private ReindexRequest prepareReindexRequest(final String fromIndex, final String toIndex) {
        ReindexRequest request = new ReindexRequest();
        request.setSourceIndices(fromIndex);
        request.setDestIndex(toIndex);
        request.setDestVersionType(VersionType.INTERNAL);
        request.setConflicts("proceed");
        request.setSourceBatchSize(searchProperties.getBatchSize());
        return request;
    }
}
