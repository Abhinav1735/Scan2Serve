package com.scan2serve.dto;

import java.util.List;

public class BillResponse {

    private Long orderId;

    private Integer tableNumber;

    private List<BillItemResponse> items;

    private Double subtotal;

    private Double gst;

    private Double grandTotal;

    // =========================================================
    // PAYMENT STATUS
    // =========================================================

    private String paymentStatus;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BillResponse() {
    }


    // =========================================================
    // GETTERS
    // =========================================================

    public Long getOrderId() {
        return orderId;
    }


    public Integer getTableNumber() {
        return tableNumber;
    }


    public List<BillItemResponse> getItems() {
        return items;
    }


    public Double getSubtotal() {
        return subtotal;
    }


    public Double getGst() {
        return gst;
    }


    public Double getGrandTotal() {
        return grandTotal;
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }


    // =========================================================
    // SETTERS
    // =========================================================

    public void setOrderId(
            Long orderId
    ) {

        this.orderId = orderId;
    }


    public void setTableNumber(
            Integer tableNumber
    ) {

        this.tableNumber = tableNumber;
    }


    public void setItems(
            List<BillItemResponse> items
    ) {

        this.items = items;
    }


    public void setSubtotal(
            Double subtotal
    ) {

        this.subtotal = subtotal;
    }


    public void setGst(
            Double gst
    ) {

        this.gst = gst;
    }


    public void setGrandTotal(
            Double grandTotal
    ) {

        this.grandTotal = grandTotal;
    }


    public void setPaymentStatus(
            String paymentStatus
    ) {

        this.paymentStatus = paymentStatus;
    }
}