package com.lineate.elastic.api.demo.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineate.elastic.api.demo.ElasticApp;

public class SearchApp extends ElasticApp {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    protected static Product parseProductFromJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Product.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
