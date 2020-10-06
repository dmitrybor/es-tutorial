package com.lineate.elastic.index;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IndexApp {
    public static void main(String[] args) {

        final String indexAlias = "test-index";
        final String indexConfigFileName = "test-index.json";

        ElasticIndexApi elasticIndexApi = new ElasticIndexApi();

        String newIndexName = indexAlias + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        elasticIndexApi.createIndex(newIndexName, indexConfigFileName);

        if (elasticIndexApi.checkIndexExists(indexAlias)) {
            String oldIndexName = elasticIndexApi.getIndexNameByAlias(indexAlias);
            elasticIndexApi.removeAliasFromIndex(oldIndexName, indexAlias);
        }

        elasticIndexApi.addAliasToIndex(newIndexName, indexAlias);

        elasticIndexApi.dispose();
    }
}
