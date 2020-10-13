package com.lineate.elastic.api.demo;

import com.lineate.elastic.api.ElasticDocApi;
import com.lineate.elastic.api.ElasticIndexApi;
import com.lineate.elastic.configuration.SearchProperties;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FastReindexingApp extends ElasticApp {

    private static final String indexName = "product";
    private static final String indexConfigFileName = "product-index-2.json";
    private static final String newIndexContentFileName = "products-bulk.json";


    public static void main(String[] args) throws IOException {
        try (RestHighLevelClient client = createElasticClient()) {
            SearchProperties searchProperties = createSearchProperties();
            ElasticIndexApi elasticIndexApi = new ElasticIndexApi(client);
            ElasticDocApi elasticDocApi = new ElasticDocApi(client, searchProperties);

            String newIndexRealName = indexName + ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            elasticIndexApi.createIndex(newIndexRealName, indexConfigFileName);

            if (elasticIndexApi.checkIndexExists(indexName)) {
                String oldIndexRealName = indexName;
                if (elasticIndexApi.checkIndexHasAlias(indexName, indexName)) {
                    oldIndexRealName = elasticIndexApi.getIndexNameByAlias(indexName);
                }
                elasticDocApi.reindex(oldIndexRealName, newIndexRealName);
                elasticIndexApi.deleteIndex(oldIndexRealName);
            } else {
                elasticDocApi.bulkIndexFromNdJsonFile(newIndexRealName, newIndexContentFileName);
            }
            elasticIndexApi.addAliasToIndex(newIndexRealName, indexName);
        }
    }
}
