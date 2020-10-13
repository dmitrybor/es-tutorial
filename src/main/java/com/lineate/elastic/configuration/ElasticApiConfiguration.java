package com.lineate.elastic.configuration;

import com.lineate.elastic.api.ElasticDocApi;
import com.lineate.elastic.api.ElasticIndexApi;
import com.lineate.elastic.api.ElasticTaskApi;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticApiConfiguration {

    @Bean
    public ElasticIndexApi getElasticIndexApi(RestHighLevelClient client) {
        return new ElasticIndexApi(client);
    }

    @Bean
    public ElasticDocApi getElasticDocApi(RestHighLevelClient client, SearchProperties searchProperties) {
        return new ElasticDocApi(client, searchProperties);
    }

    @Bean
    public ElasticTaskApi getElasticTaskApi(RestHighLevelClient client) {
        return new ElasticTaskApi(client);
    }
}
