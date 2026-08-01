package com.scan2serve.dto;

public class MenuRequest {

    private String name;
    private String description;
    private Double price;
    private Boolean available;
    private Long categoryId;

    public MenuRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}