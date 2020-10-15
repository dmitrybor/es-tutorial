package com.lineate.elastic.api;

import com.lineate.elastic.exception.ElasticActionFailedException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ElasticSearchApi {

    private final RestHighLevelClient client;

    public ElasticSearchApi(RestHighLevelClient client) {
        this.client = client;
    }

    protected List<String> performSearchQuery(final String index, final QueryBuilder queryBuilder) throws IOException {
        return performSearchQuery(index, queryBuilder, Collections.emptyList());
    }

    protected List<String> performSearchQuery(final String index, final QueryBuilder queryBuilder,
                                              final List<FieldSortSetting> sortSettings) throws IOException {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.fetchSource(true);
        sortSettings.forEach(fieldSortSetting ->
                sourceBuilder.sort(
                        new FieldSortBuilder(fieldSortSetting.getFieldName()).order(fieldSortSetting.getSortOrder())
                )
        );
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse.status() != RestStatus.OK) {
            throw new ElasticActionFailedException("Could not get search results from cluster.");
        }
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsString)
                .collect(Collectors.toList());

    }
}
