package com.scan2serve.service;

import com.scan2serve.dto.BillItemResponse;
import com.scan2serve.dto.BillResponse;
import com.scan2serve.dto.KitchenOrderItemResponse;
import com.scan2serve.dto.KitchenOrderResponse;
import com.scan2serve.dto.OrderRequest;

import com.scan2serve.entity.Cart;
import com.scan2serve.entity.Order;
import com.scan2serve.entity.OrderItem;

import com.scan2serve.enums.OrderItemStatus;
import com.scan2serve.enums.OrderStatus;

import com.scan2serve.repository.CartRepository;
import com.scan2serve.repository.OrderItemRepository;
import com.scan2serve.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    // CUSTOMER - PLACE ORDER
    // =========================================================

    public Order placeOrder(
            OrderRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Order Request is required"
            );
        }


        Integer tableNumber =
                request.getTableNumber();


        if (tableNumber == null) {

            throw new RuntimeException(
                    "Table Number is required"
            );
        }


        List<Cart> cartItems =
                cartRepository.findByTableNumber(
                        tableNumber
                );


        if (cartItems.isEmpty()) {

            throw new RuntimeException(
                    "Cart is Empty"
            );
        }


        List<OrderStatus> closedStatuses =
                List.of(
                        OrderStatus.PAID,
                        OrderStatus.CANCELLED
                );


        Order order =
                orderRepository
                        .findFirstByTableNumberAndStatusNotInOrderByIdDesc(
                                tableNumber,
                                closedStatuses
                        )
                        .orElse(null);


        if (order == null) {

            order = new Order();

            order.setTableNumber(
                    tableNumber
            );

            order.setStatus(
                    OrderStatus.PENDING
            );

            order.setOrderTime(
                    LocalDateTime.now()
            );

            order.setTotalAmount(
                    0.0
            );

            order =
                    orderRepository.save(
                            order
                    );
        }


        for (Cart cart : cartItems) {

            OrderItem item =
                    new OrderItem();


            item.setOrder(
                    order
            );


            item.setMenu(
                    cart.getMenu()
            );


            item.setQuantity(
                    cart.getQuantity()
            );


            item.setStatus(
                    OrderItemStatus.ORDER_PLACED
            );


            double price =
                    cart.getMenu().getPrice()
                            *
                            cart.getQuantity();


            item.setPrice(
                    price
            );


            orderItemRepository.save(
                    item
            );
        }


        List<OrderItem> allItems =
                orderItemRepository.findByOrder(
                        order
                );


        double total = 0.0;


        for (OrderItem item : allItems) {

            if (item.getPrice() != null) {

                total +=
                        item.getPrice();
            }
        }


        order.setTotalAmount(
                total
        );


        order =
                orderRepository.save(
                        order
                );


        synchronizeOrderStatus(
                order
        );


        cartRepository.deleteByTableNumber(
                tableNumber
        );


        return order;
    }



    // =========================================================
    // CUSTOMER - GET CURRENT ACTIVE ORDER
    // =========================================================

    public Order getCurrentOrder(
            Integer tableNumber
    ) {

        if (tableNumber == null) {

            throw new RuntimeException(
                    "Table Number is required"
            );
        }


        List<OrderStatus> closedStatuses =
                List.of(
                        OrderStatus.PAID,
                        OrderStatus.CANCELLED
                );


        return orderRepository
                .findFirstByTableNumberAndStatusNotInOrderByIdDesc(
                        tableNumber,
                        closedStatuses
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "No Active Order Found for this Table"
                        )
                );
    }



    // =========================================================
    // CUSTOMER - GET ORDER STATUS
    // =========================================================

    public OrderStatus getOrderStatus(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        return order.getStatus();
    }



    // =========================================================
    // ADMIN - GET ALL ORDERS
    // =========================================================

    public List<Order> getAllOrders() {

        return orderRepository.findAll();
    }



    // =========================================================
    // ADMIN - GET ORDER BY ID
    // =========================================================

    public Order getOrderById(
            Long id
    ) {

        return orderRepository.findById(
                        id
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order Not Found"
                        )
                );
    }



    // =========================================================
    // ADMIN - UPDATE ORDER STATUS
    // =========================================================

    public Order updateStatus(
            Long id,
            OrderStatus status
    ) {

        if (status == null) {

            throw new RuntimeException(
                    "Order Status is required"
            );
        }


        Order order =
                orderRepository.findById(
                                id
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        order.setStatus(
                status
        );


        return orderRepository.save(
                order
        );
    }



    // =========================================================
    // ADMIN - GET ORDERS BY STATUS
    // =========================================================

    public List<Order> getOrdersByStatus(
            OrderStatus status
    ) {

        return orderRepository.findByStatus(
                status
        );
    }



    // =========================================================
    // KITCHEN - GET ACTIVE ORDERS
    // =========================================================

    public List<KitchenOrderResponse> getKitchenOrders() {

        List<OrderStatus> kitchenStatuses =
                List.of(
                        OrderStatus.PENDING,
                        OrderStatus.PREPARING,
                        OrderStatus.READY
                );


        List<Order> orders =
                orderRepository.findByStatusIn(
                        kitchenStatuses
                );


        List<KitchenOrderResponse> response =
                new ArrayList<>();


        for (Order order : orders) {

            List<OrderItem> orderItems =
                    orderItemRepository.findByOrder(
                            order
                    );


            List<KitchenOrderItemResponse> items =
                    new ArrayList<>();


            for (OrderItem item : orderItems) {

                KitchenOrderItemResponse itemResponse =
                        new KitchenOrderItemResponse(
                                item.getId(),
                                item.getMenu().getId(),
                                item.getMenu().getName(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getStatus()
                        );


                items.add(
                        itemResponse
                );
            }


            KitchenOrderResponse orderResponse =
                    new KitchenOrderResponse(
                            order.getId(),
                            order.getTableNumber(),
                            order.getStatus(),
                            order.getOrderTime(),
                            order.getTotalAmount(),
                            items
                    );


            response.add(
                    orderResponse
            );
        }


        return response;
    }



    // =========================================================
    // KITCHEN - UPDATE ITEM STATUS
    // =========================================================

    public OrderItem updateItemStatus(
            Long itemId,
            OrderItemStatus newStatus
    ) {

        if (newStatus == null) {

            throw new IllegalArgumentException(
                    "New item status cannot be null"
            );
        }


        OrderItem currentItem =
                orderItemRepository.findById(
                                itemId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Item Not Found"
                                )
                        );


        OrderItemStatus currentStatus =
                currentItem.getStatus();


        validateStatusTransition(
                currentStatus,
                newStatus
        );


        currentItem.setStatus(
                newStatus
        );


        currentItem =
                orderItemRepository.save(
                        currentItem
                );


        if (
                newStatus
                        ==
                        OrderItemStatus.SERVED
        ) {

            currentItem =
                    mergeServedItems(
                            currentItem
                    );
        }


        synchronizeOrderStatus(
                currentItem.getOrder()
        );


        return currentItem;
    }



    // =========================================================
    // KITCHEN - UPDATE ITEM STATUS WITH ORDER ID
    // =========================================================

    public OrderItem updateItemStatus(
            Long orderId,
            Long itemId,
            OrderItemStatus newStatus
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        OrderItem item =
                orderItemRepository
                        .findByIdAndOrder(
                                itemId,
                                order
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Item Not Found for this Order"
                                )
                        );


        return updateItemStatus(
                item.getId(),
                newStatus
        );
    }



    // =========================================================
    // MERGE SERVED ITEMS
    // =========================================================

    private OrderItem mergeServedItems(
            OrderItem currentItem
    ) {

        Long menuId =
                currentItem
                        .getMenu()
                        .getId();


        List<OrderItem> sameItems =
                orderItemRepository
                        .findByOrderAndMenuId(
                                currentItem.getOrder(),
                                menuId
                        );


        List<OrderItem> servedItems =
                new ArrayList<>();


        for (OrderItem item : sameItems) {

            if (
                    item.getStatus()
                            ==
                            OrderItemStatus.SERVED
            ) {

                servedItems.add(
                        item
                );
            }
        }


        if (servedItems.size() <= 1) {

            return currentItem;
        }


        OrderItem mergeTarget =
                servedItems.get(0);


        for (OrderItem item : servedItems) {

            if (
                    item.getId()
                            <
                            mergeTarget.getId()
            ) {

                mergeTarget = item;
            }
        }


        int mergedQuantity = 0;


        double mergedPrice = 0.0;


        for (OrderItem item : servedItems) {

            if (item.getQuantity() != null) {

                mergedQuantity +=
                        item.getQuantity();
            }


            if (item.getPrice() != null) {

                mergedPrice +=
                        item.getPrice();
            }
        }


        mergeTarget.setQuantity(
                mergedQuantity
        );


        mergeTarget.setPrice(
                mergedPrice
        );


        mergeTarget.setStatus(
                OrderItemStatus.SERVED
        );


        mergeTarget =
                orderItemRepository.save(
                        mergeTarget
                );


        for (OrderItem item : servedItems) {

            if (
                    !item.getId()
                            .equals(
                                    mergeTarget.getId()
                            )
            ) {

                orderItemRepository.delete(
                        item
                );
            }
        }


        return mergeTarget;
    }



    // =========================================================
    // VALIDATE ITEM STATUS TRANSITION
    // =========================================================

    private void validateStatusTransition(
            OrderItemStatus currentStatus,
            OrderItemStatus newStatus
    ) {

        if (newStatus == null) {

            throw new IllegalArgumentException(
                    "New item status cannot be null"
            );
        }


        if (currentStatus == null) {

            if (
                    newStatus
                            !=
                            OrderItemStatus.ORDER_PLACED
            ) {

                throw new IllegalStateException(
                        "Invalid initial order item status"
                );
            }


            return;
        }


        if (currentStatus == newStatus) {

            return;
        }


        if (
                currentStatus
                        ==
                        OrderItemStatus.ORDER_PLACED
                        &&
                        newStatus
                                ==
                                OrderItemStatus.PREPARING
        ) {

            return;
        }


        if (
                currentStatus
                        ==
                        OrderItemStatus.PREPARING
                        &&
                        newStatus
                                ==
                                OrderItemStatus.READY
        ) {

            return;
        }


        if (
                currentStatus
                        ==
                        OrderItemStatus.READY
                        &&
                        newStatus
                                ==
                                OrderItemStatus.SERVED
        ) {

            return;
        }


        throw new IllegalStateException(
                "Invalid status transition: "
                        +
                        currentStatus
                        +
                        " -> "
                        +
                        newStatus
        );
    }



    // =========================================================
    // SYNCHRONIZE PARENT ORDER STATUS
    // =========================================================

    private void synchronizeOrderStatus(
            Order order
    ) {

        List<OrderItem> items =
                orderItemRepository.findByOrder(
                        order
                );


        if (items.isEmpty()) {

            return;
        }


        boolean hasOrderPlaced = false;

        boolean hasPreparing = false;

        boolean hasReady = false;

        boolean hasServed = false;


        for (OrderItem item : items) {

            OrderItemStatus status =
                    item.getStatus();


            if (status == null) {

                hasOrderPlaced = true;

                continue;
            }


            if (
                    status
                            ==
                            OrderItemStatus.ORDER_PLACED
            ) {

                hasOrderPlaced = true;

            } else if (
                    status
                            ==
                            OrderItemStatus.PREPARING
            ) {

                hasPreparing = true;

            } else if (
                    status
                            ==
                            OrderItemStatus.READY
            ) {

                hasReady = true;

            } else if (
                    status
                            ==
                            OrderItemStatus.SERVED
            ) {

                hasServed = true;
            }
        }


        OrderStatus newOrderStatus;


        if (hasOrderPlaced) {

            newOrderStatus =
                    OrderStatus.PENDING;

        } else if (hasPreparing) {

            newOrderStatus =
                    OrderStatus.PREPARING;

        } else if (hasReady) {

            newOrderStatus =
                    OrderStatus.READY;

        } else if (hasServed) {

            newOrderStatus =
                    OrderStatus.SERVED;

        } else {

            newOrderStatus =
                    OrderStatus.PENDING;
        }


        if (
                order.getStatus()
                        !=
                        newOrderStatus
        ) {

            order.setStatus(
                    newOrderStatus
            );


            orderRepository.save(
                    order
            );
        }
    }



    // =========================================================
    // BILL DESK - GET ACTIVE ORDERS
    // =========================================================

    public List<Order> getBillDeskOrders() {

        List<OrderStatus> closedStatuses =
                List.of(
                        OrderStatus.PAID,
                        OrderStatus.CANCELLED
                );


        return orderRepository.findByStatusNotIn(
                closedStatuses
        );
    }



    // =========================================================
    // BILL DESK - MARK ORDER AS PAID
    // =========================================================

    public Order markOrderAsPaid(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        if (
                order.getStatus()
                        ==
                        OrderStatus.PAID
        ) {

            throw new RuntimeException(
                    "Order is already PAID"
            );
        }


        if (
                order.getStatus()
                        ==
                        OrderStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Cancelled order cannot be marked as PAID"
            );
        }


        order.setStatus(
                OrderStatus.PAID
        );


        return orderRepository.save(
                order
        );
    }



    // =========================================================
    // BILL DESK - SEARCH BY ORDER ID
    // =========================================================

    public Order searchBillByOrderId(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Bill Not Found"
                                )
                        );


        if (
                order.getStatus()
                        !=
                        OrderStatus.PAID
        ) {

            throw new RuntimeException(
                    "Paid Bill Not Found"
            );
        }


        return order;
    }



    // =========================================================
    // BILL DESK - SEARCH BY TABLE NUMBER
    // =========================================================

    public List<Order> searchBillsByTableNumber(
            Integer tableNumber
    ) {

        if (tableNumber == null) {

            throw new IllegalArgumentException(
                    "Table Number is required"
            );
        }


        return orderRepository
                .findByStatus(
                        OrderStatus.PAID
                )
                .stream()
                .filter(
                        order ->
                                order.getTableNumber() != null
                                        &&
                                        order.getTableNumber()
                                                .equals(
                                                        tableNumber
                                                )
                )
                .toList();
    }



    // =========================================================
    // BILL DESK - SEARCH BY DATE
    // =========================================================

    public List<Order> searchBillsByDate(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        if (
                startDateTime == null
                        ||
                        endDateTime == null
        ) {

            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }


        return orderRepository
                .findByStatus(
                        OrderStatus.PAID
                )
                .stream()
                .filter(
                        order -> {

                            if (
                                    order.getOrderTime()
                                            == null
                            ) {

                                return false;
                            }


                            return
                                    !order.getOrderTime()
                                            .isBefore(
                                                    startDateTime
                                            )
                                            &&
                                            order.getOrderTime()
                                                    .isBefore(
                                                            endDateTime
                                                    );
                        }
                )
                .toList();
    }



    // =========================================================
    // BILL DESK - OLD BILLS
    // =========================================================

    public List<Order> searchOldBills(
            Long orderId,
            Integer tableNumber,
            LocalDate date
    ) {

        LocalDateTime startDateTime = null;

        LocalDateTime endDateTime = null;


        if (date != null) {

            startDateTime =
                    date.atStartOfDay();


            endDateTime =
                    date.plusDays(1)
                            .atStartOfDay();
        }


        return orderRepository.searchOldBills(
                OrderStatus.PAID,
                orderId,
                tableNumber,
                startDateTime,
                endDateTime
        );
    }



    // =========================================================
    // CUSTOMER - GENERATE BILL
    // =========================================================
    //
    // ALL ITEMS ARE SHOWN.
    //
    // ORDER_PLACED -> shown, not billable
    // PREPARING    -> shown, not billable
    // READY        -> shown, billable
    // SERVED       -> shown, billable
    //
    // PAYMENT STATUS:
    // The current OrderStatus is also exposed as
    // paymentStatus in BillResponse.
    //
    // =========================================================

    public BillResponse generateBill(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        return buildCustomerBill(
                order
        );
    }



    // =========================================================
    // CUSTOMER - GENERATE BILL WITH TABLE VALIDATION
    // =========================================================

    public BillResponse generateBill(
            Long orderId,
            Integer tableNumber
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        if (tableNumber == null) {

            throw new RuntimeException(
                    "Table Number is required"
            );
        }


        if (
                !tableNumber.equals(
                        order.getTableNumber()
                )
        ) {

            throw new RuntimeException(
                    "You are not authorized to view this bill"
            );
        }


        return buildCustomerBill(
                order
        );
    }



    // =========================================================
    // BILL DESK - GENERATE BILL
    // =========================================================
    //
    // ALL ITEMS ARE SHOWN.
    //
    // Only READY + SERVED affect the amount.
    //
    // =========================================================

    public BillResponse generateBillForBillDesk(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        return buildBillForBillDesk(
                order
        );
    }



    // =========================================================
    // CUSTOMER BILL BUILDER
    // =========================================================

    private BillResponse buildCustomerBill(
            Order order
    ) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(
                        order
                );


        List<BillItemResponse> items =
                new ArrayList<>();


        double subtotal = 0.0;


        for (OrderItem item : orderItems) {

            if (
                    item.getQuantity() == null
                            ||
                            item.getQuantity() <= 0
            ) {

                continue;
            }


            if (item.getPrice() == null) {

                continue;
            }


            if (item.getMenu() == null) {

                continue;
            }


            double unitPrice =
                    item.getPrice()
                            /
                            item.getQuantity();


            BillItemResponse billItem =
                    new BillItemResponse();


            billItem.setItemName(
                    item.getMenu().getName()
            );


            billItem.setQuantity(
                    item.getQuantity()
            );


            billItem.setUnitPrice(
                    unitPrice
            );


            billItem.setTotalPrice(
                    item.getPrice()
            );


            // =================================================
            // PRESERVE ACTUAL KITCHEN STATUS
            // =================================================

            if (item.getStatus() != null) {

                billItem.setStatus(
                        item.getStatus().name()
                );

            } else {

                billItem.setStatus(
                        OrderItemStatus.ORDER_PLACED.name()
                );
            }


            // =================================================
            // ALL ITEMS ARE DISPLAYED
            // =================================================

            items.add(
                    billItem
            );


            // =================================================
            // ONLY READY + SERVED ARE BILLABLE
            // =================================================

            if (
                    item.getStatus()
                            ==
                            OrderItemStatus.READY
                            ||
                            item.getStatus()
                                    ==
                                    OrderItemStatus.SERVED
            ) {

                subtotal +=
                        item.getPrice();
            }
        }


        // =====================================================
        // GST
        // =====================================================

        double gst =
                subtotal * 0.05;


        // =====================================================
        // GRAND TOTAL
        // =====================================================

        double grandTotal =
                subtotal + gst;


        // =====================================================
        // CREATE BILL RESPONSE
        // =====================================================

        BillResponse bill =
                new BillResponse();


        bill.setOrderId(
                order.getId()
        );


        bill.setTableNumber(
                order.getTableNumber()
        );


        bill.setItems(
                items
        );


        bill.setSubtotal(
                subtotal
        );


        bill.setGst(
                gst
        );


        bill.setGrandTotal(
                grandTotal
        );


        // =====================================================
        // PAYMENT STATUS
        // =====================================================
        //
        // Bill Desk marks the Order as PAID.
        //
        // The customer-side bill receives that status
        // through paymentStatus.
        //
        // For a normal active order, this will normally be:
        //
        // PENDING
        // PREPARING
        // READY
        // SERVED
        //
        // After payment:
        //
        // PAID
        //
        // =====================================================

        if (order.getStatus() != null) {

            bill.setPaymentStatus(
                    order.getStatus().name()
            );

        } else {

            bill.setPaymentStatus(
                    "UNPAID"
            );
        }


        return bill;
    }



    // =========================================================
    // BILL DESK BILL BUILDER
    // =========================================================
    //
    // IMPORTANT:
    //
    // ALL ITEMS ARE DISPLAYED.
    //
    // ORDER_PLACED -> displayed, NOT billable
    // PREPARING    -> displayed, NOT billable
    // READY        -> displayed, BILLABLE
    // SERVED       -> displayed, BILLABLE
    //
    // The real status is preserved.
    //
    // =========================================================

    private BillResponse buildBillForBillDesk(
            Order order
    ) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(
                        order
                );


        List<BillItemResponse> items =
                new ArrayList<>();


        double subtotal = 0.0;


        for (OrderItem item : orderItems) {

            // -------------------------------------------------
            // VALIDATION
            // -------------------------------------------------

            if (
                    item.getQuantity() == null
                            ||
                            item.getQuantity() <= 0
            ) {

                continue;
            }


            if (item.getPrice() == null) {

                continue;
            }


            if (item.getMenu() == null) {

                continue;
            }


            // -------------------------------------------------
            // UNIT PRICE
            // -------------------------------------------------

            double unitPrice =
                    item.getPrice()
                            /
                            item.getQuantity();


            // -------------------------------------------------
            // CREATE BILL ITEM
            // -------------------------------------------------

            BillItemResponse billItem =
                    new BillItemResponse();


            billItem.setItemName(
                    item.getMenu().getName()
            );


            billItem.setQuantity(
                    item.getQuantity()
            );


            billItem.setUnitPrice(
                    unitPrice
            );


            billItem.setTotalPrice(
                    item.getPrice()
            );


            // -------------------------------------------------
            // PRESERVE ACTUAL STATUS
            // -------------------------------------------------

            if (item.getStatus() != null) {

                billItem.setStatus(
                        item.getStatus().name()
                );

            } else {

                billItem.setStatus(
                        OrderItemStatus.ORDER_PLACED.name()
                );
            }


            // -------------------------------------------------
            // ADD EVERY ITEM TO BILL DESK
            // -------------------------------------------------

            items.add(
                    billItem
            );


            // -------------------------------------------------
            // ONLY READY + SERVED ARE BILLABLE
            // -------------------------------------------------

            if (
                    item.getStatus()
                            ==
                            OrderItemStatus.READY
                            ||
                            item.getStatus()
                                    ==
                                    OrderItemStatus.SERVED
            ) {

                subtotal +=
                        item.getPrice();
            }
        }


        // =====================================================
        // GST
        // =====================================================

        double gst =
                subtotal * 0.05;


        // =====================================================
        // GRAND TOTAL
        // =====================================================

        double grandTotal =
                subtotal + gst;


        // =====================================================
        // CREATE BILL RESPONSE
        // =====================================================

        BillResponse bill =
                new BillResponse();


        bill.setOrderId(
                order.getId()
        );


        bill.setTableNumber(
                order.getTableNumber()
        );


        bill.setItems(
                items
        );


        bill.setSubtotal(
                subtotal
        );


        bill.setGst(
                gst
        );


        bill.setGrandTotal(
                grandTotal
        );


        return bill;
    }



    // =========================================================
    // KITCHEN - GET ITEMS OF ONE ORDER
    // =========================================================

    public List<KitchenOrderItemResponse> getOrderItems(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(
                        order
                );


        List<KitchenOrderItemResponse> response =
                new ArrayList<>();


        for (OrderItem item : orderItems) {

            KitchenOrderItemResponse itemResponse =
                    new KitchenOrderItemResponse(
                            item.getId(),
                            item.getMenu().getId(),
                            item.getMenu().getName(),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getStatus()
                    );


            response.add(
                    itemResponse
            );
        }


        return response;
    }
}