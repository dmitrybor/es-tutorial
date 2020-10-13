package com.lineate.elastic.api.demo;

import com.lineate.elastic.configuration.SearchProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticApp {
    protected static final String esHost = "localhost";
    protected static final int esPort = 9200;
    protected static final int batchSize = 100;
    protected static final String trackingTaskRequestInterval = "PT10S";


    protected static RestHighLevelClient createElasticClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, "http")));
    }

    protected static SearchProperties createSearchProperties() {
        SearchProperties searchProperties = new SearchProperties();
        searchProperties.setHost(esHost);
        searchProperties.setPort(esPort);
        searchProperties.setBatchSize(batchSize);
        searchProperties.setTrackingTaskRequestInterval(trackingTaskRequestInterval);
        return searchProperties;
    }
}
