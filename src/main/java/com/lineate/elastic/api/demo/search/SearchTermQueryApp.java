package com.lineate.elastic.api.demo.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineate.elastic.api.ElasticSearchApi;
import com.lineate.elastic.api.demo.ElasticApp;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SearchTermQueryApp extends ElasticApp {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        final String indexName = "product";
        try (RestHighLevelClient client = createElasticClient()) {
            ElasticSearchApi elasticSearchApi = new ElasticSearchApi(client);
            System.out.println("Active products");
            List<String> activeProducts = elasticSearchApi.performTermQuery(indexName, "is_active", "true");
            activeProducts
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | is_active: " + product.isActive()));

            System.out.println("-----------------------------");
            System.out.println("Products with alcohol");
            List<String> productsWithAlcohol = elasticSearchApi.performTermQuery(indexName, "tags", "Alcohol");
            productsWithAlcohol
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products with Soup or Cake tags");
            List<String> productsWithSoupOrCakeTags = elasticSearchApi.performMultipleTermsQuery(
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
                    elasticSearchApi.performGetDocumentById(indexName, List.of("1", "7", "29"));
            productsWithSpecifiedIds
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println("Name: " + product.getName()));

            System.out.println("-----------------------------");
            System.out.println("Products with in_stock values from 1 to 5 including");
            List<String> productsWihInStockValuesFrom1To5 =
                    elasticSearchApi.performRangeQuery(indexName, "in_stock", "1", "5");
            productsWihInStockValuesFrom1To5
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | in_stock: " + product.getInStock()));

            System.out.println("-----------------------------");
            System.out.println("Products created between 2010/01/01 and  2010/12/31");
            List<String> productsCreatedWithinSpecifiedDates =
                    elasticSearchApi.performRangeQuery(indexName, "created", "2010/01/01", "2010/12/31");
            productsCreatedWithinSpecifiedDates
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("name: " + product.getName() + " | created: " + product.getCreated()));

            System.out.println("-----------------------------");
            System.out.println("Products that have at least one tag");
            List<String> productsWithTags =
                    elasticSearchApi.performExistsQuery(indexName, "tags");
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
                    elasticSearchApi.performPrefixQuery(indexName, "tags", "vege");
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
                    elasticSearchApi.performWildcardQuery(indexName, "tags", "veg*ble");
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
                    elasticSearchApi.performRegexQuery(indexName, "tags", "veg[a-zA-Z]+ble");
            productsWithTagsMatchingRegexp
                    .stream()
                    .map(SearchTermQueryApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                                    + " | Tags: " + String.join(", ", product.getTags())));
        }
    }

    private static Product parseProductFromJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Product.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
