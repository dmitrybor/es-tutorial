package com.lineate.elastic.api.demo.search;

import com.lineate.elastic.api.ElasticSearchMatchApi;
import com.lineate.elastic.api.FieldSortSetting;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.sort.SortOrder;

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
            System.out.println("Products that have \"Wine\" OR \"Red\" OR both in the name " +
                    "sorted by in_stock (desc) and then by date created (asc)");
            List<String> productsWithWineOrRedInNameSorted =
                    elasticSearchMatchApi.performMatchQuery(indexName, "name", "wine Red", Operator.OR,
                            List.of(new FieldSortSetting("in_stock", SortOrder.DESC),
                                    new FieldSortSetting("created", SortOrder.ASC)));
            productsWithWineOrRedInNameSorted
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name:" + product.getName()
                            + " | in_stock: " + product.getInStock()
                            + " | created: " + product.getCreated()));

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
            System.out.println("Products that have \"Wine\" AND \"Red\" in the name " +
                    "sorted by in_stock (desc) and date created (asc)");
            List<String> productsWithWineAndRedInNameSorted =
                    elasticSearchMatchApi.performMatchQuery(indexName, "name", "wine red", Operator.AND,
                            List.of(new FieldSortSetting("in_stock", SortOrder.DESC),
                                    new FieldSortSetting("created", SortOrder.ASC)));
            productsWithWineAndRedInNameSorted
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product ->
                            System.out.println("Name: " + product.getName()
                            + " | in_stock: " + product.getInStock()
                            + " | created: " + product.getCreated()));

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
            System.out.println("Products that have the word \"Meat\" in the name or tags or both");
            List<String> productsWithMeatInNameOrTags =
                    elasticSearchMatchApi.performMultiMatchQuery(indexName, List.of("name", "tags"), "meat");
            productsWithMeatInNameOrTags
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println("Name: " + product.getName()
                            + " | Tags: " + String.join(", ", product.getTags())));

            System.out.println("-----------------------------");
            System.out.println("Products that have the word \"Meat\" in the name or tags or both " +
                    "sorted by ");
            List<String> productsWithMeatInNameOrTagsSorted =
                    elasticSearchMatchApi.performMultiMatchQuery(indexName, List.of("name", "tags"), "meat");
            productsWithMeatInNameOrTagsSorted
                    .stream()
                    .map(SearchApp::parseProductFromJson)
                    .filter(Objects::nonNull)
                    .forEach(product -> System.out.println("Name: " + product.getName()
                            + " | Tags: " + String.join(", ", product.getTags())));
        }
    }
}
