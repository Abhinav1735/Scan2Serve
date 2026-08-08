package com.scan2serve.service;

import com.scan2serve.dto.*;
import com.scan2serve.entity.Cart;
import com.scan2serve.entity.Order;
import com.scan2serve.entity.OrderItem;
import com.scan2serve.enums.OrderStatus;
import com.scan2serve.repository.CartRepository;
import com.scan2serve.repository.OrderItemRepository;
import com.scan2serve.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    // ============================
    // Customer - Place Order
    // ============================

    public Order placeOrder(OrderRequest request) {

        List<Cart> cartItems = cartRepository.findByTableNumber(request.getTableNumber());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is Empty");
        }

        Order order = new Order();

        order.setTableNumber(request.getTableNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());

        double total = 0;

        order = orderRepository.save(order);

        for (Cart cart : cartItems) {

            OrderItem item = new OrderItem();

            item.setOrder(order);
            item.setMenu(cart.getMenu());
            item.setQuantity(cart.getQuantity());

            double price = cart.getMenu().getPrice() * cart.getQuantity();

            item.setPrice(price);

            total += price;

            orderItemRepository.save(item);
        }

        order.setTotalAmount(total);

        orderRepository.save(order);

        cartRepository.deleteByTableNumber(request.getTableNumber());

        return order;
    }

    // ============================
    // Admin
    // ============================

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
    }

    public Order updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getKitchenOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    // ============================
    // Bill Generation
    // ============================

    public BillResponse generateBill(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        List<BillItemResponse> items = new ArrayList<>();

        double subtotal = 0;

        for (OrderItem item : orderItems) {

            double unitPrice = item.getPrice() / item.getQuantity();

            items.add(
                    new BillItemResponse(
                            item.getMenu().getName(),
                            item.getQuantity(),
                            unitPrice,
                            item.getPrice()
                    )
            );

            subtotal += item.getPrice();
        }

        double gst = subtotal * 0.05;
        double grandTotal = subtotal + gst;

        BillResponse bill = new BillResponse();

        bill.setOrderId(order.getId());
        bill.setTableNumber(order.getTableNumber());
        bill.setItems(items);
        bill.setSubtotal(subtotal);
        bill.setGst(gst);
        bill.setGrandTotal(grandTotal);

        return bill;
    }
}