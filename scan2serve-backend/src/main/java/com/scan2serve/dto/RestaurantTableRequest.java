package com.scan2serve.dto;

public class RestaurantTableRequest {

    private Integer tableNumber;
    private Boolean active;

    public RestaurantTableRequest() {
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}