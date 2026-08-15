package com.scan2serve.dto;

public class BillItemResponse {

    private String itemName;

    private Integer quantity;

    private Double unitPrice;

    private Double totalPrice;

    private String status;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public BillItemResponse() {
    }


    public BillItemResponse(
            String itemName,
            Integer quantity,
            Double unitPrice,
            Double totalPrice
    ) {

        this.itemName = itemName;

        this.quantity = quantity;

        this.unitPrice = unitPrice;

        this.totalPrice = totalPrice;
    }


    public BillItemResponse(
            String itemName,
            Integer quantity,
            Double unitPrice,
            Double totalPrice,
            String status
    ) {

        this.itemName = itemName;

        this.quantity = quantity;

        this.unitPrice = unitPrice;

        this.totalPrice = totalPrice;

        this.status = status;
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public String getItemName() {
        return itemName;
    }


    public Integer getQuantity() {
        return quantity;
    }


    public Double getUnitPrice() {
        return unitPrice;
    }


    public Double getTotalPrice() {
        return totalPrice;
    }


    public String getStatus() {
        return status;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setItemName(
            String itemName
    ) {

        this.itemName = itemName;
    }


    public void setQuantity(
            Integer quantity
    ) {

        this.quantity = quantity;
    }


    public void setUnitPrice(
            Double unitPrice
    ) {

        this.unitPrice = unitPrice;
    }


    public void setTotalPrice(
            Double totalPrice
    ) {

        this.totalPrice = totalPrice;
    }


    public void setStatus(
            String status
    ) {

        this.status = status;
    }
}