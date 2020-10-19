package com.lineate.elastic.api;

import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ElasticSearchTermApi extends ElasticSearchApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchTermApi.class);

    public ElasticSearchTermApi(RestHighLevelClient client) {
        super(client);
    }

    public List<String> performTermQuery(final String index, final String field, final String value) {
        return performTermQuery(index, field, value, Collections.emptyList());
    }

    public List<String> performTermQuery(final String index, final String field,
                                         final String value, List<FieldSortSetting> sortSettings) {

        try {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value);
            return performSearchQuery(index, termQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple term query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple term query.", ex);
        }
    }


    public double performTermQueryWithSumAggregation(final String index, final String field,
                                                     final String value, final String numericFieldToSumOver) {
        try {
            final String aggregationName = "sum_aggregation";
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value);
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum(aggregationName);
            sumAggregationBuilder.field(numericFieldToSumOver);
            Aggregations aggregations = performSearchQueryWithAggregation(index, termQueryBuilder, sumAggregationBuilder);
            Sum sumAggregation = aggregations.get(aggregationName);
            return sumAggregation.getValue();
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple term query with sum aggregation.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple term query with sum aggregation.", ex);
        }
    }

    public Terms performTermQueryWithTermsAggregation(final String index, final String field,
                                                      final String value, final String fieldNameForAggregation) {
        try {
            final String aggregationName = "sum_aggregation";
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value);
            TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(aggregationName);
            termsAggregationBuilder.field(fieldNameForAggregation);
            termsAggregationBuilder.missing("missing_value");
            Aggregations aggregations = performSearchQueryWithAggregation(index, termQueryBuilder, termsAggregationBuilder);
            return aggregations.get(aggregationName);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple term query with terms aggregation.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple term query with terms aggregation.", ex);
        }
    }

    public List<String> performMultipleTermsQuery(final String index, final String field, final List<String> values) {
        return performMultipleTermsQuery(index, field, values, Collections.emptyList());
    }

    public List<String> performMultipleTermsQuery(final String index, final String field,
                                                  final List<String> values, final List<FieldSortSetting> sortSettings) {
        try {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(field, values);
            return performSearchQuery(index, termsQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing multi term query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing multi term query.", ex);
        }
    }

    public List<String> performGetDocumentById(final String index, final List<String> ids) {
        return performGetDocumentById(index, ids, Collections.emptyList());
    }

    public List<String> performGetDocumentById(final String index, final List<String> ids,
                                               final List<FieldSortSetting> sortSettings) {
        try {
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            ids.forEach(idsQueryBuilder::addIds);
            return performSearchQuery(index, idsQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing fetch by id query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing fetch by id query.", ex);
        }
    }

    public double performGetDocumentByIdWithSumAggregation(final String index, final List<String> ids,
                                                           final String numericFieldToSumOver) {
        try {
            final String aggregationName = "sum_aggregation";
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            ids.forEach(idsQueryBuilder::addIds);
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum(aggregationName);
            sumAggregationBuilder.field(numericFieldToSumOver);
            Aggregations aggregations = performSearchQueryWithAggregation(index, idsQueryBuilder, sumAggregationBuilder);
            Sum sumAggregation = aggregations.get(aggregationName);
            return sumAggregation.getValue();
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing fetch by id query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing fetch by id query.", ex);
        }
    }

    public double performTermQueryWithAvgAggregation(final String index, final List<String> ids,
                                                     final String numericFieldToAvgOver) {
        try {
            final String aggregationName = "avg_aggregation";
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            ids.forEach(idsQueryBuilder::addIds);
            AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg(aggregationName);
            avgAggregationBuilder.field(numericFieldToAvgOver);
            Aggregations aggregations = performSearchQueryWithAggregation(index, idsQueryBuilder, avgAggregationBuilder);
            Avg sumAggregation = aggregations.get(aggregationName);
            return sumAggregation.getValue();
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple term query with avg aggregation.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple term query with avg aggregation.", ex);
        }
    }

    public List<String> performRangeQuery(final String index, final String field,
                                          final String lowerBound, final String upperBound) {
        return performRangeQuery(index, field, lowerBound, upperBound, Collections.emptyList());
    }

    public List<String> performRangeQuery(final String index, final String field,
                                          final String lowerBound, final String upperBound,
                                          final List<FieldSortSetting> sortSettings) {
        try {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field);
            rangeQueryBuilder.gte(lowerBound);
            rangeQueryBuilder.lte(upperBound);
            return performSearchQuery(index, rangeQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing range search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing range search query.", ex);
        }
    }

    public List<String> performExistsQuery(final String index, final String field) {
        return performExistsQuery(index, field, Collections.emptyList());
    }

    public List<String> performExistsQuery(final String index, final String field,
                                           final List<FieldSortSetting> sortSettings) {
        try {
            ExistsQueryBuilder queryBuilder = QueryBuilders.existsQuery(field);
            return performSearchQuery(index, queryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing exists search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing exists search query.", ex);
        }
    }

    public List<String> performPrefixQuery(final String index, final String field, final String prefix) {
        return performPrefixQuery(index, field, prefix, Collections.emptyList());
    }

    public List<String> performPrefixQuery(final String index, final String field, final String prefix,
                                           final List<FieldSortSetting> sortSettings) {
        try {
            PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(field, prefix);
            return performSearchQuery(index, prefixQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing prefix search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing prefix search query.", ex);
        }
    }

    public List<String> performWildcardQuery(final String index, final String field, final String wildcard) {
        return performWildcardQuery(index, field, wildcard, Collections.emptyList());
    }

    public List<String> performWildcardQuery(final String index, final String field, final String wildcard,
                                             final List<FieldSortSetting> sortSettings) {
        try {
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(field, wildcard);
            return performSearchQuery(index, wildcardQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing wildcard search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing wildcard search query.", ex);
        }
    }

    public List<String> performRegexQuery(final String index, final String field, final String regex) {
        return performRegexQuery(index, field, regex, Collections.emptyList());
    }

    public List<String> performRegexQuery(final String index, final String field, final String regex,
                                          final List<FieldSortSetting> sortSettings) {
        try {
            RegexpQueryBuilder regexpQueryBuilder = QueryBuilders.regexpQuery(field, regex);
            return performSearchQuery(index, regexpQueryBuilder, sortSettings);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing regex search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing regex search query.", ex);
        }
    }
}
