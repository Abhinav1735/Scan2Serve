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

    // =========================================================
    // Customer - Place Order
    // =========================================================

    public Order placeOrder(OrderRequest request) {

        Integer tableNumber = request.getTableNumber();

        // -----------------------------------------------------
        // 1. Get cart items for this table
        // -----------------------------------------------------

        List<Cart> cartItems = cartRepository.findByTableNumber(tableNumber);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is Empty");
        }

        // -----------------------------------------------------
        // 2. Check if this table already has an active order
        // -----------------------------------------------------

        Order order = orderRepository
                .findFirstByTableNumberAndStatusNotOrderByIdDesc(
                        tableNumber,
                        OrderStatus.PAID)
                .orElse(null);

        // -----------------------------------------------------
        // 3. If no active order exists, create a new order
        // -----------------------------------------------------

        if (order == null || order.getStatus() == OrderStatus.CANCELLED) {

            order = new Order();

            order.setTableNumber(tableNumber);
            order.setStatus(OrderStatus.PENDING);
            order.setOrderTime(LocalDateTime.now());
            order.setTotalAmount(0.0);

            order = orderRepository.save(order);
        }

        // -----------------------------------------------------
        // 4. Add cart items to the SAME order
        // -----------------------------------------------------

        double newItemsTotal = 0.0;

        List<OrderItem> existingItems = orderItemRepository.findByOrder(order);

        for (Cart cart : cartItems) {

            double price = cart.getMenu().getPrice() * cart.getQuantity();

            newItemsTotal += price;

            // -------------------------------------------------
            // Check whether this menu item already exists
            // in this order
            // -------------------------------------------------

            OrderItem existingItem = null;

            for (OrderItem item : existingItems) {

                if (item.getMenu().getId()
                        .equals(cart.getMenu().getId())) {

                    existingItem = item;
                    break;
                }
            }

            // -------------------------------------------------
            // If item already exists:
            // increase quantity and total price
            // -------------------------------------------------

            if (existingItem != null) {

                existingItem.setQuantity(
                        existingItem.getQuantity()
                                + cart.getQuantity());

                existingItem.setPrice(
                        existingItem.getPrice()
                                + price);

                orderItemRepository.save(existingItem);

            }

            // -------------------------------------------------
            // Otherwise create a new OrderItem
            // -------------------------------------------------

            else {

                OrderItem item = new OrderItem();

                item.setOrder(order);
                item.setMenu(cart.getMenu());
                item.setQuantity(cart.getQuantity());
                item.setPrice(price);

                orderItemRepository.save(item);

                // Add it to the local list so that if the same
                // item appears again in this cart, we can detect it.
                existingItems.add(item);
            }
        }

        // -----------------------------------------------------
        // 5. Add new amount to existing order total
        // -----------------------------------------------------

        double oldTotal = order.getTotalAmount() == null
                ? 0.0
                : order.getTotalAmount();

        double finalTotal = oldTotal + newItemsTotal;

        order.setTotalAmount(finalTotal);

        orderRepository.save(order);

        // -----------------------------------------------------
        // 6. Clear only the cart
        // -----------------------------------------------------

        cartRepository.deleteByTableNumber(tableNumber);

        // -----------------------------------------------------
        // 7. Return SAME order
        // -----------------------------------------------------

        return order;
    }

    // =========================================================
    // Admin
    // =========================================================

    public List<Order> getAllOrders() {

        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Order Not Found"));
    }

    public Order updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order Not Found"));

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {

        return orderRepository.findByStatus(status);
    }

    public List<Order> getKitchenOrders() {

        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    // =========================================================
    // Bill Generation
    // =========================================================

    public BillResponse generateBill(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order Not Found"));

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
                            item.getPrice()));

            subtotal += item.getPrice();
        }

        // -----------------------------------------------------
        // GST
        // -----------------------------------------------------

        double gst = subtotal * 0.05;

        double grandTotal = subtotal + gst;

        // -----------------------------------------------------
        // Create Bill Response
        // -----------------------------------------------------

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