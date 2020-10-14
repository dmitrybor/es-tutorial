package com.lineate.elastic.api;

import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ElasticSearchMatchApi extends ElasticSearchApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchMatchApi.class);

    public ElasticSearchMatchApi(RestHighLevelClient client) {
        super(client);
    }

    public List<String> performMatchQuery(final String index, final String field,
                                          final String wordsToMatch, Operator operator) {
        try {
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, wordsToMatch);
            matchQueryBuilder.operator(operator);
            return performSearchQuery(index, matchQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple match query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple match query.", ex);
        }
    }

    public List<String> performMatchPhraseQuery(final String index, final String field, final String phrase) {
        try {
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(field, phrase);
            return performSearchQuery(index, matchPhraseQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing match phrase query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing match phrase query.", ex);
        }
    }

    public List<String> performMultiMatchQuery(final String index, final List<String> fields, final String wordsToMatch) {
        try {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(wordsToMatch);
            fields.forEach(multiMatchQueryBuilder::field);
            return performSearchQuery(index, multiMatchQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing multi match query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing multi match query.", ex);
        }
    }

}
