package com.lineate.elastic.api.demo;

import com.lineate.elastic.api.ElasticDocApi;
import com.lineate.elastic.api.ElasticIndexApi;
import com.lineate.elastic.api.ElasticTaskApi;
import com.lineate.elastic.configuration.SearchProperties;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.GetTaskResponse;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TaskApp extends ElasticApp {
    private static final String indexName = "product";
    private static final String indexConfigFileName = "product-index-2.json";
    private static final String newIndexContentFileName = "products-bulk.json";


    public static void main(String[] args) throws IOException, InterruptedException {
        try (RestHighLevelClient client = createElasticClient()) {
            ElasticIndexApi elasticIndexApi = new ElasticIndexApi(client);
            SearchProperties searchProperties = createSearchProperties();
            ElasticDocApi elasticDocApi = new ElasticDocApi(client, searchProperties);
            ElasticTaskApi elasticTaskApi = new ElasticTaskApi(client);

            if (!elasticIndexApi.checkIndexExists(indexName)) {
                initIndex(elasticIndexApi, elasticDocApi, indexName, newIndexContentFileName);
            }

            String newIndexRealName = indexName + ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            elasticIndexApi.createIndex(newIndexRealName, indexConfigFileName);

            String oldIndexRealName = indexName;
            if (elasticIndexApi.checkIndexHasAlias(indexName, indexName)) {
                oldIndexRealName = elasticIndexApi.getIndexNameByAlias(indexName);
            }

            String reindexTaskId = elasticDocApi.submitReindexTask(oldIndexRealName, newIndexRealName);
            Thread.sleep(100);
            GetTaskResponse taskInfoResponse = elasticTaskApi.getTaskInfo(reindexTaskId, false);

            elasticTaskApi.cancelTask(reindexTaskId);

            Thread.sleep(100);
            taskInfoResponse = elasticTaskApi.getTaskInfo(reindexTaskId, true);

            elasticIndexApi.deleteIndex(newIndexRealName);
        }
    }

    public static void initIndex(final ElasticIndexApi elasticIndexApi,
                                 final ElasticDocApi elasticDocApi,
                                 final String indexBaseName,
                                 final String indexContentFileName) throws IOException {
        String initIndexName = indexBaseName + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + "init";
        elasticIndexApi.createIndex(initIndexName, indexConfigFileName);

        elasticDocApi.bulkIndexFromNdJsonFile(initIndexName, indexContentFileName);

        elasticIndexApi.addAliasToIndex(initIndexName, indexName);
    }
}
