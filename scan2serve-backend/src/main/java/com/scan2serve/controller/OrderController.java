package com.scan2serve.controller;

import com.scan2serve.dto.BillResponse;
import com.scan2serve.dto.KitchenOrderResponse;
import com.scan2serve.dto.OrderItemStatusRequest;
import com.scan2serve.dto.OrderRequest;
import com.scan2serve.dto.OrderStatusRequest;

import com.scan2serve.entity.Order;
import com.scan2serve.entity.OrderItem;

import com.scan2serve.enums.OrderStatus;

import com.scan2serve.response.ApiResponse;
import com.scan2serve.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class OrderController {


    @Autowired
    private OrderService orderService;


    // =========================================================
    // CUSTOMER APIs
    // =========================================================


    // =========================================================
    // CUSTOMER - PLACE ORDER
    // =========================================================

    @PostMapping("/customer/order")
    public ApiResponse<Order> placeOrder(
            @Valid @RequestBody OrderRequest request
    ) {

        Order order =
                orderService.placeOrder(
                        request
                );


        return new ApiResponse<>(
                true,
                "Order Placed Successfully",
                order
        );
    }


    // =========================================================
    // CUSTOMER - BILL
    // =========================================================

    @GetMapping("/customer/bill/{orderId}")
    public ApiResponse<BillResponse> getBill(
            @PathVariable Long orderId
    ) {

        BillResponse bill =
                orderService.generateBill(
                        orderId
                );


        return new ApiResponse<>(
                true,
                "Bill Generated Successfully",
                bill
        );
    }


    // =========================================================
    // ADMIN APIs
    // =========================================================


    // =========================================================
    // ADMIN - GET ALL ORDERS
    // =========================================================

    @GetMapping("/admin/orders")
    public ApiResponse<List<Order>> getAllOrders() {

        return new ApiResponse<>(
                true,
                "Orders Fetched Successfully",
                orderService.getAllOrders()
        );
    }


    // =========================================================
    // ADMIN - GET ORDER BY ID
    // =========================================================

    @GetMapping("/admin/orders/{id}")
    public ApiResponse<Order> getOrderById(
            @PathVariable Long id
    ) {

        return new ApiResponse<>(
                true,
                "Order Found",
                orderService.getOrderById(
                        id
                )
        );
    }


    // =========================================================
    // ADMIN - UPDATE ORDER STATUS
    // =========================================================

    @PutMapping("/admin/orders/{id}/status")
    public ApiResponse<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusRequest request
    ) {

        Order order =
                orderService.updateStatus(
                        id,
                        request.getStatus()
                );


        return new ApiResponse<>(
                true,
                "Order Status Updated Successfully",
                order
        );
    }


    // =========================================================
    // ADMIN - GET ORDERS BY STATUS
    // =========================================================

    @GetMapping("/admin/orders/status/{status}")
    public ApiResponse<List<Order>> getOrdersByStatus(
            @PathVariable OrderStatus status
    ) {

        return new ApiResponse<>(
                true,
                "Orders Fetched Successfully",
                orderService.getOrdersByStatus(
                        status
                )
        );
    }


    // =========================================================
    // KITCHEN APIs
    // =========================================================


    // =========================================================
    // KITCHEN - GET ACTIVE ORDERS
    // =========================================================

    @GetMapping("/kitchen/orders")
    public ApiResponse<List<KitchenOrderResponse>> getKitchenOrders() {

        return new ApiResponse<>(
                true,
                "Kitchen Orders Fetched Successfully",
                orderService.getKitchenOrders()
        );
    }


    // =========================================================
    // KITCHEN - INDIVIDUAL ITEM STATUS
    // =========================================================

    @PutMapping(
            "/kitchen/order-items/{itemId}/status"
    )
    public ApiResponse<OrderItem> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody OrderItemStatusRequest request
    ) {

        OrderItem item =
                orderService.updateItemStatus(
                        itemId,
                        request.getStatus()
                );


        return new ApiResponse<>(
                true,
                "Order Item Status Updated Successfully",
                item
        );
    }

}