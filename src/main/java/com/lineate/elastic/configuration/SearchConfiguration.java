package com.lineate.elastic.configuration;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfiguration {

    /**
     * Create high level search client.
     *
     * @param props search properties.
     * @return client
     */
    @Bean
    public RestHighLevelClient getHighLevelClient(final SearchProperties props) {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(props.getHost(), props.getPort(), "http")
                ));
    }
}
