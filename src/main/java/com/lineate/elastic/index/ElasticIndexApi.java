package com.lineate.elastic.index;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ElasticIndexApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticIndexApi.class);

    private final RestHighLevelClient client;

    public ElasticIndexApi(RestHighLevelClient client) {
        this.client = client;
    }

    public boolean createIndex(final String indexName, final String indexConfigFileName) {

        try {
            LOGGER.info("Creating index: {}", indexName);

            final String indexConfigString;
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(indexConfigFileName)) {
                if (resourceAsStream == null) {
                    LOGGER.warn("Cannot find index config file file {}", indexConfigFileName);
                    return false;
                }
                byte[] bytes = resourceAsStream.readAllBytes();
                indexConfigString = new String(bytes, Charset.defaultCharset());
            }

            final CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            createRequest.source(indexConfigString, XContentType.JSON);

            final CreateIndexResponse createIndexResponse = client.indices().create(createRequest, RequestOptions.DEFAULT);

            if (!createIndexResponse.isAcknowledged()) {
                LOGGER.warn("Create index request failed.");
                return false;
            }
            LOGGER.info("Index was successfully created.");
            return true;

        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error during index creation", e);
            return false;
        }
    }

    public boolean checkIndexExists(final String indexName) {
        try {
            LOGGER.info("Checking index {} exists", indexName);
            final GetIndexRequest request = new GetIndexRequest(indexName);
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

            LOGGER.info("Index {} exists: {}", indexName, exists);
            return exists;

        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error occurred while checking index existence", e);
            return false;
        }
    }

    public void deleteIndex(final String indexName) {
        try {
            LOGGER.info("Deleting index: {}", indexName);

            DeleteIndexRequest request = new DeleteIndexRequest(indexName);

            AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
            if (deleteIndexResponse.isAcknowledged()) {
                LOGGER.info("Index {} deleted", indexName);
            } else {
                LOGGER.info("Could not delete index {}", indexName);
            }
        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error occurred while deleting index", e);
        }
    }

    public void addAliasToIndex(final String indexName, final String alias) {
        try {
            LOGGER.info("Adding alias {} to index {}", alias, indexName);

            IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(indexName)
                            .alias(alias);
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            request.addAliasAction(aliasAction);

            AcknowledgedResponse indicesAliasesResponse =
                    client.indices().updateAliases(request, RequestOptions.DEFAULT);

            if (indicesAliasesResponse.isAcknowledged()) {
                LOGGER.info("Alias was successfully added to index");
            } else {
                LOGGER.info("Could not add alias to index");
            }

        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error occurred while adding alias index", e);
        }
    }

    public void removeAliasFromIndex(final String indexName, final String alias) {
        try {
            LOGGER.info("Removing alias {} from index {}", alias, indexName);

            IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                            .index(indexName)
                            .alias(alias);
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            request.addAliasAction(aliasAction);

            AcknowledgedResponse indicesAliasesResponse =
                    client.indices().updateAliases(request, RequestOptions.DEFAULT);

            if (indicesAliasesResponse.isAcknowledged()) {
                LOGGER.info("Alias was successfully removed from index");
            } else {
                LOGGER.info("Could not remove alias from index");
            }
        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error occurred while removing alias from index", e);
        }
    }

    public String getIndexNameByAlias(final String alias) {
        try {
            LOGGER.info("Getting index name by alias {}", alias);

            GetAliasesRequest request = new GetAliasesRequest(alias);
            GetAliasesResponse response = client.indices().getAlias(request, RequestOptions.DEFAULT);

            if (response.status() == RestStatus.OK) {
                LOGGER.info("Successfully got index name for alias {}", alias);
                return response.getAliases().keySet().stream().findFirst().get();
            } else {
                LOGGER.info("Could not get index name for alias {}", alias);
                return null;
            }
        } catch (IOException | ElasticsearchStatusException e) {
            LOGGER.warn("Error occurred while getting aliases for index", e);
            return null;
        }
    }

}
