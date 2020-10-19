package com.lineate.elastic.api.demo.search;

import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public class SearchWithAggregationsApp extends SearchApp {

    private static final String MAX_SCORE = "max_score";
    private static final String SCORE = "_score";

    public static void main(String[] args) throws IOException {
        final String indexName = "product";
        final String aggregationName = "product_names";

        try (RestHighLevelClient client = createElasticClient()) {

            System.out.println("Search for product name in a type ahead like manner");
            String searchWord = "re win";

            String searchField = "name.partial";
            MatchQueryBuilder matchQuery = getMatchQuery(searchField, searchWord);

            String aggregationField = "name.raw";
            TermsAggregationBuilder aggregationQuery = getAggregationQuery(aggregationName, aggregationField, 10);

            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(matchQuery);
            builder.aggregation(aggregationQuery);
            builder.fetchSource(false);

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indexName);
            searchRequest.source(builder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.status() != RestStatus.OK) {
                throw new ElasticActionFailedException("Could not get search results from cluster.");
            }

            Terms aggregation = searchResponse.getAggregations().get(aggregationName);

            aggregation.getBuckets().forEach(bucket -> System.out.println(bucket.getKeyAsString()));
        }
    }

    private static MatchQueryBuilder getMatchQuery(String fieldName, String value) {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(fieldName, value);
        matchQueryBuilder.operator(Operator.AND);
        return matchQueryBuilder;
    }

    private static TermsAggregationBuilder getAggregationQuery(String aggregationName, String fieldName, int limit) {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders
                .terms(aggregationName)
                .field(fieldName)
                .subAggregation(getScoreAggregation())
                .order(BucketOrder.aggregation(MAX_SCORE, false))
                .size(limit);
        return termsAggregationBuilder;
    }

    private static AggregationBuilder getScoreAggregation() {
        return AggregationBuilders
                .max(MAX_SCORE)
                .script(new Script(SCORE));
    }
}
