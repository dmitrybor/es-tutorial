package com.lineate.elastic.api.demo.search;

import com.lineate.elastic.api.ElasticSearchTermApi;
import com.lineate.elastic.api.FieldSortSetting;
import org.elasticsearch.client.RestHighLevelClient;
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
                    .forEach(product -> System.out.println("Name: " + product.getName()));

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
