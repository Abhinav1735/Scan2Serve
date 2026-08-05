package com.scan2serve.controller;

import com.scan2serve.dto.*;
import com.scan2serve.entity.Order;
import com.scan2serve.enums.OrderStatus;
import com.scan2serve.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Customer

    @PostMapping("/customer/order")
    public Order placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/customer/bill/{orderId}")
    public BillResponse getBill(@PathVariable Long orderId) {
        return orderService.generateBill(orderId);
    }

    // Admin

    @GetMapping("/admin/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/admin/orders/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/admin/orders/{id}/status")
    public Order updateStatus(@PathVariable Long id,
                              @RequestBody OrderStatusRequest request) {

        return orderService.updateStatus(id, request.getStatus());
    }

    @GetMapping("/admin/orders/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }

    // Kitchen

    @GetMapping("/kitchen/orders")
    public List<Order> getKitchenOrders() {
        return orderService.getKitchenOrders();
    }
}