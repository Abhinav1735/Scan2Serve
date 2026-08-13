package com.scan2serve.dto;

public class CustomerMenuItemResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;

    public CustomerMenuItemResponse() {
    }

    public CustomerMenuItemResponse(Long id,
                                    String name,
                                    String description,
                                    Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
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
}