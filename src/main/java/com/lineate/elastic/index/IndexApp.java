package com.lineate.elastic.index;

import com.lineate.elastic.ElasticApp;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IndexApp extends ElasticApp {

    public static void main(String[] args) throws IOException {

        final String indexAlias = "test-index";
        final String indexConfigFileName = "test-index.json";
        try (final RestHighLevelClient client = createElasticClient()) {

            ElasticIndexApi elasticIndexApi = new ElasticIndexApi(client);

            String newIndexName = indexAlias + ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            elasticIndexApi.createIndex(newIndexName, indexConfigFileName);

            if (elasticIndexApi.checkIndexExists(indexAlias)) {
                String oldIndexName = elasticIndexApi.getIndexNameByAlias(indexAlias);
                elasticIndexApi.removeAliasFromIndex(oldIndexName, indexAlias);
            }

            elasticIndexApi.addAliasToIndex(newIndexName, indexAlias);
        }
    }
}
