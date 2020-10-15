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
import java.util.Collections;
import java.util.List;

public class ElasticSearchMatchApi extends ElasticSearchApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchMatchApi.class);

    public ElasticSearchMatchApi(RestHighLevelClient client) {
        super(client);
    }

    public List<String> performMatchQuery(final String index, final String field,
                                          final String wordsToMatch, final Operator operator) {
        return performMatchQuery(index, field, wordsToMatch, operator, Collections.emptyList());
    }

    public List<String> performMatchQuery(final String index, final String field,
                                          final String wordsToMatch, Operator operator,
                                          final List<FieldSortSetting> sortSettings) {
        try {
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, wordsToMatch);
            matchQueryBuilder.operator(operator);
            return performSearchQuery(index, matchQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple match query and sorting results.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple match query and sorting results", ex);
        }
    }

    public List<String> performMatchPhraseQuery(final String index, final String field, final String phrase) {
        return performMatchPhraseQuery(index, field, phrase, Collections.emptyList());
    }

    public List<String> performMatchPhraseQuery(final String index, final String field,
                                                final String phrase, final List<FieldSortSetting> sortSettings) {
        try {
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(field, phrase);
            return performSearchQuery(index, matchPhraseQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing match phrase query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing match phrase query.", ex);
        }
    }

    public List<String> performMultiMatchQuery(final String index, final List<String> fields, final String wordsToMatch) {
        return performMultiMatchQuery(index, fields, wordsToMatch, Collections.emptyList());
    }

    public List<String> performMultiMatchQuery(final String index, final List<String> fields,
                                               final String wordsToMatch, final List<FieldSortSetting> sortSettings) {
        try {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(wordsToMatch);
            fields.forEach(multiMatchQueryBuilder::field);
            return performSearchQuery(index, multiMatchQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing multi match query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing multi match query.", ex);
        }
    }
}
