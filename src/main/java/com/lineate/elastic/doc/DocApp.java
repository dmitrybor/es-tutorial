package com.lineate.elastic.doc;

import com.lineate.elastic.ElasticApp;
import com.lineate.elastic.index.ElasticIndexApi;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DocApp extends ElasticApp {
    public static void main(String[] args) throws IOException {
        try (final RestHighLevelClient client = createElasticClient()) {
            ElasticIndexApi elasticIndexApi = new ElasticIndexApi(client);
            ElasticDocApi elasticDocApi = new ElasticDocApi(client);

            String baseIndexName = "product";
            String firstIndexName = baseIndexName + ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String secondIndexName = baseIndexName + ZonedDateTime.now().plusSeconds(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));

            elasticIndexApi.createIndex(firstIndexName, "product-index-1.json");
            elasticDocApi.bulkIndexFromNdJsonFile(firstIndexName, "products-bulk.json");

            elasticIndexApi.createIndex(secondIndexName, "product-index-2.json");
            elasticDocApi.reindex(firstIndexName, secondIndexName);

            elasticIndexApi.deleteIndex(firstIndexName);
        }
    }
}
