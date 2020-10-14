package com.lineate.elastic.api.demo.search;

import com.lineate.elastic.api.ElasticSearchMatchApi;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SearchMatchQueryApp extends SearchApp {

    public static void main(String[] args) throws IOException {
        final String indexName = "product";
        try (RestHighLevelClient client = createElasticClient()) {
            ElasticSearchMatchApi elasticSearchMatchApi = new ElasticSearchMatchApi(client);

            System.out.println("-----------------------------");
            System.out.println("Products that have \"Wine\" OR \"Red\" OR both in the name");
            List<String> productsWithWineOrRedInName =
                    elasticSearchMatchApi.performMatchQuery(indexName, "name", "wine Red", Operator.OR);
            productsWithWineOrRedInName
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println(product.getName()));

            System.out.println("-----------------------------");
            System.out.println("Products that have \"Wine\" AND \"Red\" in the name");
            List<String> productsWithWineAndRedInName =
                    elasticSearchMatchApi.performMatchQuery(indexName, "name", "wine red", Operator.AND);
            productsWithWineAndRedInName
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println(product.getName()));

            System.out.println("-----------------------------");
            System.out.println("Products that have the phrase \"Cabernet Sauvignon\" in the name");
            List<String> productsWithPhraseInName =
                    elasticSearchMatchApi.performMatchPhraseQuery(indexName, "name", "cabernet sauvignon");
            productsWithPhraseInName
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println(product.getName()));

            System.out.println("-----------------------------");
            System.out.println("Products that have the word\"Meat\" in the name or tags or both");
            List<String> productsWithDrinkInNameOrTags =
                    elasticSearchMatchApi.performMultiMatchQuery(indexName, List.of("name", "tags"), "meat");
            productsWithDrinkInNameOrTags
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println("Name: " + product.getName()
                            + " | Tags: " + String.join(", ", product.getTags())));
        }
    }
}
