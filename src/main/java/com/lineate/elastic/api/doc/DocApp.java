package com.lineate.elastic.api.doc;

import com.lineate.elastic.ElasticApp;
import com.lineate.elastic.api.index.ElasticIndexApi;
import com.lineate.elastic.configuration.SearchProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DocApp extends ElasticApp {
    public static void main(String[] args) throws IOException {
        try (final RestHighLevelClient client = createElasticClient()) {
            ElasticIndexApi elasticIndexApi = new ElasticIndexApi(client);
            SearchProperties searchProperties = createSearchProperties();
            ElasticDocApi elasticDocApi = new ElasticDocApi(client, searchProperties);

            String baseIndexName = "product";
            String firstIndexName = baseIndexName + ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String secondIndexName = baseIndexName + ZonedDateTime.now().plusSeconds(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));

            elasticIndexApi.createIndex(firstIndexName, "product-index-1.json");
            elasticDocApi.bulkIndexFromNdJsonFile(firstIndexName, "products-bulk.json");

            elasticIndexApi.createIndex(secondIndexName, "product-index-2.json");
            elasticDocApi.reindex(firstIndexName, secondIndexName);
            elasticIndexApi.addAliasToIndex(secondIndexName, baseIndexName);
            elasticIndexApi.deleteIndex(firstIndexName);
        }
    }

    private static RestHighLevelClient createElasticClient(final int connectTimeout, final int socketTimeout) {
        RestClientBuilder builder = RestClient.builder(new HttpHost(esHost, esPort, "http"));
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout)
                        .setConnectionRequestTimeout(0));

        return new RestHighLevelClient(builder);
    }
}
