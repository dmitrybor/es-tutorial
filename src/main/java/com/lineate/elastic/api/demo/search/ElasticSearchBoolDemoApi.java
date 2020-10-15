package com.lineate.elastic.api.demo.search;

import com.lineate.elastic.api.ElasticSearchApi;
import com.lineate.elastic.api.FieldSortSetting;
import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ElasticSearchBoolDemoApi extends ElasticSearchApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchBoolDemoApi.class);

    public ElasticSearchBoolDemoApi(RestHighLevelClient client) {
        super(client);
    }

    public List<String> searchTextAndLowerBound(final String index,
                                                final String textFieldName, final String textToSearch,
                                                final String integerFieldName, final int lowerBound) {
        return searchTextAndLowerBound(index, textFieldName, textToSearch,
                integerFieldName, lowerBound, Collections.emptyList());
    }

    public List<String> searchTextAndLowerBound(final String index,
                                                final String textFieldName, final String textToSearch,
                                                final String integerFieldName, final int lowerBound,
                                                final List<FieldSortSetting> sortSettings) {
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            MatchQueryBuilder hasTextInField = QueryBuilders.matchQuery(textFieldName, textToSearch);
            boolQueryBuilder.must(hasTextInField);

            RangeQueryBuilder greaterThanLowerBound = QueryBuilders.rangeQuery(integerFieldName);
            greaterThanLowerBound.gte(lowerBound);
            boolQueryBuilder.filter(greaterThanLowerBound);

            return performSearchQuery(index, boolQueryBuilder, sortSettings);

        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing compound query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing compound query.", ex);
        }
    }

    public List<String> searchTextAndLowerBoundExcludeText(final String index,
                                                           final String searchTextFieldName, final String textToSearch,
                                                           final String integerFieldName, final int lowerBound,
                                                           final String excludeTextFieldName, final String textToExclude) {
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            MatchQueryBuilder hasTextInField = QueryBuilders.matchQuery(searchTextFieldName, textToSearch);
            boolQueryBuilder.must(hasTextInField);

            RangeQueryBuilder greaterThanLowerBound = QueryBuilders.rangeQuery(integerFieldName);
            greaterThanLowerBound.gte(lowerBound);
            boolQueryBuilder.filter(greaterThanLowerBound);

            MatchQueryBuilder excludeTextInField = QueryBuilders.matchQuery(excludeTextFieldName, textToExclude);
            boolQueryBuilder.mustNot(excludeTextInField);

            return performSearchQuery(index, boolQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing compound query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing compound query.", ex);
        }
    }

    public List<String> searchTextAndLowerBoundExcludeTextPreferText(
            final String index,
            final String searchTextFieldName, final String textToSearch,
            final String integerFieldName, final int lowerBound,
            final String excludeTextFieldName, final String textToExclude,
            final String preferTextFieldName, final String textToPrefer
    ) {
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            MatchQueryBuilder hasTextInField = QueryBuilders.matchQuery(searchTextFieldName, textToSearch);
            boolQueryBuilder.must(hasTextInField);

            RangeQueryBuilder greaterThanLowerBound = QueryBuilders.rangeQuery(integerFieldName);
            greaterThanLowerBound.gte(lowerBound);
            boolQueryBuilder.filter(greaterThanLowerBound);

            MatchQueryBuilder excludeTextInField = QueryBuilders.matchQuery(excludeTextFieldName, textToExclude);
            boolQueryBuilder.mustNot(excludeTextInField);

            MatchQueryBuilder preferTextInField = QueryBuilders.matchQuery(preferTextFieldName, textToPrefer);
            boolQueryBuilder.should(preferTextInField);

            return performSearchQuery(index, boolQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing compound query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing compound query.", ex);
        }
    }
}
