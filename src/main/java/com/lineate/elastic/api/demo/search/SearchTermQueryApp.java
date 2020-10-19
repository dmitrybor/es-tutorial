package com.lineate.elastic.api.demo.search;

import com.lineate.elastic.api.ElasticSearchTermApi;
import com.lineate.elastic.api.FieldSortSetting;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SearchTermQueryApp extends SearchApp {

    public static void main(String[] args) throws IOException {
        final String indexName = "product";
        try (RestHighLevelClient client = createElasticClient()) {
            ElasticSearchTermApi elasticSearchTermApi = new ElasticSearchTermApi(client);
            System.out.println("Active products");
            List<String> activeProducts = elasticSearchTermApi.performTermQuery(indexName, "is_active", "true");
            activeProducts
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | is_active: " + product.isActive()));

            System.out.println("-----------------------------");
            System.out.println("Active products sorted by price (desc) and then by date created (asc)");
            List<String> activeProductsSorted =
                    elasticSearchTermApi.performTermQuery(indexName, "is_active", "true",
                            List.of(new FieldSortSetting("price", SortOrder.DESC),
                                    new FieldSortSetting("created", SortOrder.ASC)));
            activeProductsSorted
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | is_active: " + product.isActive()
                            + " | price: " + product.getPrice() + " | created: " + product.getCreated()));

            System.out.println("-----------------------------");
            System.out.println("Total price of all active products");
            System.out.println(elasticSearchTermApi.performTermQueryWithSumAggregation(indexName, "is_active",
                    "true", "price"));

            System.out.println("-----------------------------");
            System.out.println("Active products aggregated by tags");
            Terms productsWithAlcoholByStatus =
                    elasticSearchTermApi.performTermQueryWithTermsAggregation(indexName, "is_active", "true",
                            "tags");
            productsWithAlcoholByStatus.getBuckets().forEach(
                    bucket -> System.out.println("Bucket Key: " + bucket.getKeyAsString()
                            + " | Bucket Doc count: " + bucket.getDocCount())
            );

            System.out.println("-----------------------------");
            System.out.println("Products created in 2010 aggregated by active status and then by tags");

            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("created");
            rangeQueryBuilder.gte("2010/01/01");
            rangeQueryBuilder.lte("2010/12/31");

            final String activeStatusAggregationName = "product_active";
            TermsAggregationBuilder statusAggregationBuilder = AggregationBuilders.terms(activeStatusAggregationName);
            statusAggregationBuilder.field("is_active");

            final String tagAggregationName = "tag_aggregation";
            TermsAggregationBuilder tagsAggregationBuilder = AggregationBuilders.terms(tagAggregationName);
            tagsAggregationBuilder.field("tags");
            tagsAggregationBuilder.missing("no tags");

            statusAggregationBuilder.subAggregation(tagsAggregationBuilder);

            Aggregations productsIn2010WithAggregations =
                    elasticSearchTermApi.performSearchQueryWithAggregation(indexName, rangeQueryBuilder, statusAggregationBuilder);
            Terms byStatus = productsIn2010WithAggregations.get(activeStatusAggregationName);
            byStatus.getBuckets().forEach(statusBucket -> {
                System.out.println("Status: " + statusBucket.getKeyAsString() + " | Doc count: " + statusBucket.getDocCount());
                Terms byTags = statusBucket.getAggregations().get(tagAggregationName);
                byTags.getBuckets().forEach(tagBucket ->
                        System.out.println("    Tag: " + tagBucket.getKeyAsString() + " | Doc count: " + tagBucket.getDocCount()));
            });

            System.out.println("-----------------------------");
            System.out.println("Products with alcohol");
            List<String> productsWithAlcohol = elasticSearchTermApi.performTermQuery(indexName, "tags", "Alcohol");
            productsWithAlcohol
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products with Soup or Cake tags");
            List<String> productsWithSoupOrCakeTags = elasticSearchTermApi.performMultipleTermsQuery(
                    indexName, "tags", List.of("soup", "cake"));
            productsWithSoupOrCakeTags
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products with ids 1, 7, 29");
            List<String> productsWithSpecifiedIds =
                    elasticSearchTermApi.performGetDocumentById(indexName, List.of("1", "7", "29"));
            productsWithSpecifiedIds
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println("Name: " + product.getName()
                    + " | in_stock: " + product.getInStock()
                    + " | Price: " + product.getPrice()));

            System.out.println("-----------------------------");
            System.out.println("Total in_stock value for products with ids 1, 7, 29");
            System.out.println(elasticSearchTermApi.performGetDocumentByIdWithSumAggregation(indexName, List.of("1", "7", "29"), "in_stock"));

            System.out.println("-----------------------------");
            System.out.println("Average price for products with ids 1, 7, 29");
            System.out.println(elasticSearchTermApi.performTermQueryWithAvgAggregation(indexName, List.of("1", "7", "29"), "price"));

            System.out.println("-----------------------------");
            System.out.println("Products with in_stock values from 1 to 5 including");
            List<String> productsWihInStockValuesFrom1To5 =
                    elasticSearchTermApi.performRangeQuery(indexName, "in_stock", "1", "5");
            productsWihInStockValuesFrom1To5
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | in_stock: " + product.getInStock()));

            System.out.println("-----------------------------");
            System.out.println("Products created between 2010/01/01 and  2010/12/31");
            List<String> productsCreatedWithinSpecifiedDates =
                    elasticSearchTermApi.performRangeQuery(indexName, "created", "2010/01/01", "2010/12/31");
            productsCreatedWithinSpecifiedDates
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("name: " + product.getName() + " | created: " + product.getCreated()));

            System.out.println("-----------------------------");
            System.out.println("Products that have at least one tag");
            List<String> productsWithTags =
                    elasticSearchTermApi.performExistsQuery(indexName, "tags");
            productsWithTags
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products that have tags startig with \"vege\"");
            List<String> productsWithVegeTags =
                    elasticSearchTermApi.performPrefixQuery(indexName, "tags", "vege");
            productsWithVegeTags
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products that have tags matching the \"veg*ble\" wildcard");
            List<String> productsWithTagsMatchingWildcard =
                    elasticSearchTermApi.performWildcardQuery(indexName, "tags", "veg*ble");
            productsWithTagsMatchingWildcard
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products that have tags matching the \"veg[a-zA-Z]+ble\" regular expression");
            List<String> productsWithTagsMatchingRegexp =
                    elasticSearchTermApi.performRegexQuery(indexName, "tags", "veg[a-zA-Z]+ble");
            productsWithTagsMatchingRegexp
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));
        }
    }
}
