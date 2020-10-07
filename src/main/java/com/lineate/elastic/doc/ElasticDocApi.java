package com.lineate.elastic.doc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lineate.elastic.index.ElasticIndexApi;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
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

    public ElasticDocApi(RestHighLevelClient client) {
        this.client = client;
    }

    public void reindex(final String fromIndex, final String toIndex) {
        try {
            LOGGER.info("Reindexing documents from {} to {}", fromIndex, toIndex);

            ReindexRequest request = new ReindexRequest();
            request.setSourceIndices(fromIndex);
            request.setDestIndex(toIndex);
            request.setDestVersionType(VersionType.INTERNAL);
            request.setConflicts("proceed");
            request.setSourceBatchSize(100);

            BulkByScrollResponse bulkResponse =
                    client.reindex(request, RequestOptions.DEFAULT);

            if (bulkResponse.isTimedOut()) {
                LOGGER.info("Could not reindex documents from {} to {}", fromIndex, toIndex);
            } else {
                LOGGER.info("Successfully reindexed {} documents from {} to {}", bulkResponse.getTotal(), fromIndex, toIndex);
            }
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while reindexing", e);
        }
    }

    public void bulkIndexFromNdJsonFile(final String indexName, final String ndJsonFileName) {
        try {
            LOGGER.info("Indexing documents into {} from file {}", indexName, ndJsonFileName);
            BulkRequest bulkRequest;
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(ndJsonFileName);
                 InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, Charset.defaultCharset());
                 BufferedReader reader = new BufferedReader(inputStreamReader)
            ) {
                if (resourceAsStream == null) {
                    LOGGER.warn("Cannot index content json file file {}", ndJsonFileName);
                    return;
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
                LOGGER.info("{} Documents were successfully indexed from file", bulkResponse.getItems().length);
            } else {
                LOGGER.info("Could not index documents form file");
            }

        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error during bulk indexing", e);
        }
    }
}
