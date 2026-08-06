package com.scan2serve.dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull(message = "Table Number is required")
    private Integer tableNumber;

    public OrderRequest() {
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }
}