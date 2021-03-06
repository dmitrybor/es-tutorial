package com.lineate.elastic.api;

import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

public class ElasticIndexApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticIndexApi.class);

    private final RestHighLevelClient client;

    public ElasticIndexApi(RestHighLevelClient client) {
        this.client = client;
    }

    public void createIndex(final String indexName, final String indexConfigFileName) {

        try {
            LOGGER.info("Creating index: {}", indexName);

            final String indexConfigString;
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(indexConfigFileName)) {
                if (resourceAsStream == null) {
                    LOGGER.warn("Cannot find index config file file {}", indexConfigFileName);
                    throw new ElasticActionFailedException("Cannot find index config file file");
                }
                byte[] bytes = resourceAsStream.readAllBytes();
                indexConfigString = new String(bytes, Charset.defaultCharset());
            }

            final CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            createRequest.source(indexConfigString, XContentType.JSON);

            final CreateIndexResponse createIndexResponse = client.indices().create(createRequest, RequestOptions.DEFAULT);

            if (!createIndexResponse.isAcknowledged()) {
                LOGGER.warn("Index creation request failed.");
                throw new ElasticActionFailedException("Index creation request failed.");
            }
            LOGGER.info("Index was successfully created.");

        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred during index creation.", e);
            throw new ElasticActionFailedException("Error occurred during index creation.", e);
        }
    }

    public boolean checkIndexExists(final String indexName) {
        try {
            LOGGER.info("Checking index {} exists", indexName);
            final GetIndexRequest request = new GetIndexRequest(indexName);
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

            LOGGER.info("Index {} exists: {}", indexName, exists);
            return exists;

        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while checking index existence.", e);
            throw new ElasticActionFailedException("Error occurred while checking index existence.", e);
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
                throw new ElasticActionFailedException("Could not delete index.");
            }
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while deleting index.", e);
            throw new ElasticActionFailedException("Error occurred while deleting index.", e);
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
                LOGGER.info("Could not add alias to index.");
                throw new ElasticActionFailedException("Could not add alias to index.");
            }

        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while adding alias index.", e);
            throw new ElasticActionFailedException("Error occurred while adding alias index.", e);
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
                LOGGER.info("Alias was successfully removed from index.");
            } else {
                LOGGER.info("Could not remove alias from index.");
                throw new ElasticActionFailedException("Could not remove alias from index.");
            }
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while removing alias from index.", e);
            throw new ElasticActionFailedException("Error occurred while removing alias from index.", e);
        }
    }

    public boolean checkIndexHasAlias(final String indexName, final String alias) {
        try {
            GetAliasesRequest request = new GetAliasesRequest();
            request.indices(indexName);
            request.aliases(alias);
            return client.indices().existsAlias(request, RequestOptions.DEFAULT);
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while checking alias for index.", e);
            throw new ElasticActionFailedException("Error occurred while checking alias for index.", e);
        }
    }

    public String getIndexNameByAlias(final String alias) {
        try {
            LOGGER.info("Getting index name by alias {}", alias);

            GetSettingsRequest request = new GetSettingsRequest().indices(alias);
            request.names("index.provided_name");

            GetSettingsResponse response = client.indices().getSettings(request, RequestOptions.DEFAULT);

            Iterator<String> indexNamesIterator = response.getIndexToSettings().keysIt();
            if (indexNamesIterator.hasNext()) {
                return indexNamesIterator.next();
            }
            LOGGER.info("Could not get index name by alias.");
            throw new ElasticActionFailedException("Could not get index name by alias.");

        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("Error occurred while getting aliases for index.", e);
            throw new ElasticActionFailedException("Error occurred while getting aliases for index.", e);
        }
    }
}
