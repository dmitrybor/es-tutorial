package com.lineate.elastic.index;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IndexApp {

    private static final String esHost = "localhost";
    private static final int esPort = 9200;

    public static void main(String[] args) throws IOException {

        final String indexAlias = "test-index";
        final String indexConfigFileName = "test-index.json";
        final RestHighLevelClient client = createElasticClient();

        ElasticIndexApi elasticIndexApi = new ElasticIndexApi(client);

        String newIndexName = indexAlias + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        elasticIndexApi.createIndex(newIndexName, indexConfigFileName);

        if (elasticIndexApi.checkIndexExists(indexAlias)) {
            String oldIndexName = elasticIndexApi.getIndexNameByAlias(indexAlias);
            elasticIndexApi.removeAliasFromIndex(oldIndexName, indexAlias);
        }

        elasticIndexApi.addAliasToIndex(newIndexName, indexAlias);

        client.close();
    }

    private static RestHighLevelClient createElasticClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHost, esPort, "http"))
        );
    }
}
