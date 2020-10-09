package com.lineate.elastic.api.task;

import com.lineate.elastic.ElasticApp;
import com.lineate.elastic.api.doc.ElasticDocApi;
import com.lineate.elastic.api.index.ElasticIndexApi;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.tasks.TaskInfo;

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
            ElasticDocApi elasticDocApi = new ElasticDocApi(client);
            ElasticTaskApi elasticTaskApi = new ElasticTaskApi(client);


            if (!elasticIndexApi.checkIndexExists(indexName)) {
                initIndex(elasticIndexApi, elasticDocApi, indexName, newIndexContentFileName);
            }

            String newIndexRealName = indexName + ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            if (!elasticIndexApi.createIndex(newIndexRealName, indexConfigFileName)) {
                return;
            }

            String oldIndexRealName = indexName;
            if (elasticIndexApi.checkIndexHasAlias(indexName, indexName)) {
                oldIndexRealName = elasticIndexApi.getIndexNameByAlias(indexName);
            }

            String reindexTaskId = elasticDocApi.submitReindexTask(oldIndexRealName, newIndexRealName);
            Thread.sleep(100);
            TaskInfo taskInfo = elasticTaskApi.getTaskInfo(reindexTaskId, false);

            elasticTaskApi.cancelTask(reindexTaskId);

            Thread.sleep(100);
            taskInfo = elasticTaskApi.getTaskInfo(reindexTaskId, true);

            elasticIndexApi.deleteIndex(newIndexRealName);
        }
    }

    public static boolean initIndex(final ElasticIndexApi elasticIndexApi,
                                    final ElasticDocApi elasticDocApi,
                                    final String indexBaseName,
                                    final String indexContentFileName) throws IOException {
        String initIndexName = indexBaseName + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + "init";
        if (!elasticIndexApi.createIndex(initIndexName, indexConfigFileName)) {
            return false;
        }

        elasticDocApi.bulkIndexFromNdJsonFile(initIndexName, indexContentFileName);

        if (!elasticIndexApi.addAliasToIndex(initIndexName, indexName)) {
            return false;
        }
        return true;
    }
}
