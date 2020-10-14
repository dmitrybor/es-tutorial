package com.lineate.elastic.api.demo.search;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Product {
    private String name;
    private int price;

    @JsonAlias("in_stock")
    private int inStock;

    private int sold;
    private List<String> tags;
    private String description;

    @JsonAlias("is_active")
    private boolean isActive;

    private String created;

    public Product() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @JsonProperty("in_stock")
    public int getInStock() {
        return inStock;
    }

    public void setInStock(int inStock) {
        this.inStock = inStock;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("is_active")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return price == product.price &&
                inStock == product.inStock &&
                sold == product.sold &&
                isActive == product.isActive &&
                Objects.equals(name, product.name) &&
                Objects.equals(tags, product.tags) &&
                Objects.equals(description, product.description) &&
                Objects.equals(created, product.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, inStock, sold, tags, description, isActive, created);
    }
}
