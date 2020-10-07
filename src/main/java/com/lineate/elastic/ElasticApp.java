package com.lineate.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticApp {
    private static final String esHost = "localhost";
    private static final int esPort = 9200;

    protected static RestHighLevelClient createElasticClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHost, esPort, "http"))
        );
    }
}
