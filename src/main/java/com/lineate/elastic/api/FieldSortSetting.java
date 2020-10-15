package com.lineate.elastic.api;

import org.elasticsearch.search.sort.SortOrder;

public class FieldSortSetting {
    private String fieldName;
    private SortOrder sortOrder;

    public FieldSortSetting() {
    }

    public FieldSortSetting(String fieldName, SortOrder sortOrder) {
        this.fieldName = fieldName;
        this.sortOrder = sortOrder;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
}
