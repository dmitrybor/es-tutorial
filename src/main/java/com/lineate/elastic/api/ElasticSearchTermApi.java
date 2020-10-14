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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ElasticSearchTermApi extends ElasticSearchApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchTermApi.class);

    public ElasticSearchTermApi(RestHighLevelClient client) {
        super(client);
    }

    public List<String> performTermQuery(final String index, final String field, final String value) {

        try {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value);
            return performSearchQuery(index, termQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing simple term query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing simple term query.", ex);
        }
    }

    public List<String> performMultipleTermsQuery(final String index, final String field, final List<String> values) {
        try {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(field, values);
            return performSearchQuery(index, termsQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing multi term query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing multi term query.", ex);
        }
    }

    public List<String> performGetDocumentById(final String index, final List<String> ids) {
        try {
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            ids.forEach(idsQueryBuilder::addIds);
            return performSearchQuery(index, idsQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing fetch by id query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing fetch by id query.", ex);
        }
    }

    public List<String> performRangeQuery(final String index, final String field,
                                          final String lowerBound, final String upperBound) {
        try {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field);
            rangeQueryBuilder.gte(lowerBound);
            rangeQueryBuilder.lte(upperBound);
            return performSearchQuery(index, rangeQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing range search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing range search query.", ex);
        }
    }

    public List<String> performExistsQuery(final String index, final String field) {
        try {
            ExistsQueryBuilder queryBuilder = QueryBuilders.existsQuery(field);
            return performSearchQuery(index, queryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing exists search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing exists search query.", ex);
        }
    }

    public List<String> performPrefixQuery(final String index, final String field, final String prefix) {
        try {
            PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(field, prefix);
            return performSearchQuery(index, prefixQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing prefix search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing prefix search query.", ex);
        }
    }

    public List<String> performWildcardQuery(final String index, final String field, final String wildcard) {
        try {
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(field, wildcard);
            return performSearchQuery(index, wildcardQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing wildcard search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing wildcard search query.", ex);
        }
    }

    public List<String> performRegexQuery(final String index, final String field, final String regex) {
        try {
            RegexpQueryBuilder regexpQueryBuilder = QueryBuilders.regexpQuery(field, regex);
            return performSearchQuery(index, regexpQueryBuilder);
        } catch (IOException | ElasticsearchException ex) {
            LOGGER.warn("Error occurred while performing regex search query.", ex);
            throw new ElasticActionFailedException("Error occurred while performing regex search query.", ex);
        }
    }
}
