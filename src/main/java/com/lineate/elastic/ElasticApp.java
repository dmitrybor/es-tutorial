package com.lineate.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticApp {
    private static final String esHost = "localhost";
    private static final int esPort = 9200;

    protected static RestHighLevelClient createElasticClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(esHost, esPort, "http"));
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(10000)
                        .setSocketTimeout(6000000)
                        .setConnectionRequestTimeout(0));

        return new RestHighLevelClient(builder);
    }
}
