package com.lineate.elastic.api.demo.search;

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SearchBooleanQueryApp extends SearchApp {

    public static void main(String[] args) throws IOException {
        final String indexName = "product";
        try (RestHighLevelClient client = createElasticClient()) {
            ElasticSearchBoolDemoApi elasticSearchBoolDemoApi = new ElasticSearchBoolDemoApi(client);

            System.out.println("Products that have \"Coffe\" in the name and at least 20 in stock");
            List<String> products = elasticSearchBoolDemoApi.searchTextAndLowerBound(indexName,
                    "name", "coffee", "in_stock", 20);
            products
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | In Stock: " + product.getInStock()));

            System.out.println("-----------------------------");
            System.out.println("Products that have \"Coffe\" and don't have \"Chocolate\" in the name and at least 20 in stock");
            products = elasticSearchBoolDemoApi.searchTextAndLowerBoundExcludeText(indexName,
                    "name", "coffee",
                    "in_stock", 20,
                    "name", "chocolate");

            products.stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | In Stock: " + product.getInStock()));

            System.out.println("-----------------------------");
            System.out.println("Products that have \"Coffe\" and don't have \"Chocolate\" in the name " +
                    "and at least 20 in stock. Prioritized by having \"Foam\" in the name");
            products = elasticSearchBoolDemoApi.searchTextAndLowerBoundExcludeTextPreferText(indexName,
                    "name", "coffee",
                    "in_stock", 20,
                    "name", "chocolate",
                    "name", "foam");

            products.stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName() + " | In Stock: " + product.getInStock()));
        }
    }
}
