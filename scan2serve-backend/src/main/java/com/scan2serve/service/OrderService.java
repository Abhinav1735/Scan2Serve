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

        Integer tableNumber =
                request.getTableNumber();


        // =====================================================
        // GET CART
        // =====================================================

        List<Cart> cartItems =
                cartRepository.findByTableNumber(
                        tableNumber
                );


        // =====================================================
        // CHECK CART
        // =====================================================

        if (cartItems.isEmpty()) {

            throw new RuntimeException(
                    "Cart is Empty"
            );
        }


        // =====================================================
        // FIND EXISTING ACTIVE ORDER
        // =====================================================

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


        // =====================================================
        // CREATE NEW ORDER IF NO ACTIVE ORDER EXISTS
        // =====================================================

        if (order == null) {

            order =
                    new Order();


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


        // =====================================================
        // ADD CART ITEMS TO SAME ORDER
        // =====================================================

        for (Cart cart : cartItems) {

            OrderItem item =
                    new OrderItem();


            // =================================================
            // ORDER
            // =================================================

            item.setOrder(
                    order
            );


            // =================================================
            // MENU
            // =================================================

            item.setMenu(
                    cart.getMenu()
            );


            // =================================================
            // QUANTITY
            // =================================================

            item.setQuantity(
                    cart.getQuantity()
            );


            // =================================================
            // NEW ITEM STATUS
            // =================================================

            item.setStatus(
                    OrderItemStatus.ORDER_PLACED
            );


            // =================================================
            // PRICE
            // =================================================

            double price =
                    cart.getMenu().getPrice()
                            * cart.getQuantity();


            item.setPrice(
                    price
            );


            // =================================================
            // SAVE ITEM
            // =================================================

            orderItemRepository.save(
                    item
            );
        }


        // =====================================================
        // RECALCULATE COMPLETE ORDER TOTAL
        // =====================================================

        List<OrderItem> allItems =
                orderItemRepository.findByOrder(
                        order
                );


        double total =
                0.0;


        for (OrderItem item : allItems) {

            if (
                    item.getPrice() != null
            ) {

                total +=
                        item.getPrice();
            }
        }


        // =====================================================
        // UPDATE ORDER TOTAL
        // =====================================================

        order.setTotalAmount(
                total
        );


        order =
                orderRepository.save(
                        order
                );


        // =====================================================
        // SYNCHRONIZE ORDER STATUS
        // =====================================================

        synchronizeOrderStatus(
                order
        );


        // =====================================================
        // CLEAR CART
        // =====================================================

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

        Order order =
                orderRepository.findById(
                                id
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        // =====================================================
        // PAID ORDER CANNOT BE REOPENED
        // =====================================================

        if (
                order.getStatus()
                        == OrderStatus.PAID
        ) {

            throw new IllegalStateException(
                    "Paid order cannot be reopened"
            );
        }


        // =====================================================
        // CANCELLED ORDER CANNOT BE MODIFIED
        // =====================================================

        if (
                order.getStatus()
                        == OrderStatus.CANCELLED
        ) {

            throw new IllegalStateException(
                    "Cancelled order cannot be modified"
            );
        }


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
    // KITCHEN - GET ACTIVE KITCHEN ORDERS
    // =========================================================

    public List<KitchenOrderResponse> getKitchenOrders() {

        // =====================================================
        // ONLY ACTIVE ORDERS
        // =====================================================

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


        // =====================================================
        // CONVERT EACH ORDER
        // =====================================================

        for (Order order : orders) {

            List<OrderItem> orderItems =
                    orderItemRepository.findByOrder(
                            order
                    );


            List<KitchenOrderItemResponse> items =
                    new ArrayList<>();


            // =================================================
            // CONVERT ORDER ITEMS
            // =================================================

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


            // =================================================
            // CREATE ORDER RESPONSE
            // =================================================

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
    // KITCHEN - UPDATE INDIVIDUAL ITEM STATUS
    // =========================================================

    public OrderItem updateItemStatus(
            Long itemId,
            OrderItemStatus newStatus
    ) {

        // =====================================================
        // FIND CURRENT ITEM
        // =====================================================

        OrderItem currentItem =
                orderItemRepository.findById(
                                itemId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Item Not Found"
                                )
                        );


        // =====================================================
        // GET PARENT ORDER
        // =====================================================

        Order order =
                currentItem.getOrder();


        // =====================================================
        // PAID ORDER CANNOT BE MODIFIED
        // =====================================================

        if (
                order.getStatus()
                        == OrderStatus.PAID
        ) {

            throw new IllegalStateException(
                    "Cannot update item status because the order is already paid"
            );
        }


        // =====================================================
        // CANCELLED ORDER CANNOT BE MODIFIED
        // =====================================================

        if (
                order.getStatus()
                        == OrderStatus.CANCELLED
        ) {

            throw new IllegalStateException(
                    "Cannot update item status because the order is cancelled"
            );
        }


        // =====================================================
        // VALIDATE STATUS TRANSITION
        // =====================================================

        OrderItemStatus currentStatus =
                currentItem.getStatus();


        validateStatusTransition(
                currentStatus,
                newStatus
        );


        // =====================================================
        // UPDATE STATUS
        // =====================================================

        currentItem.setStatus(
                newStatus
        );


        // =====================================================
        // IF NOT SERVED
        // =====================================================

        if (
                newStatus
                        != OrderItemStatus.SERVED
        ) {

            currentItem =
                    orderItemRepository.save(
                            currentItem
                    );


            synchronizeOrderStatus(
                    order
            );


            return currentItem;
        }


        // =====================================================
        // SAVE CURRENT ITEM AS SERVED
        // =====================================================

        currentItem =
                orderItemRepository.save(
                        currentItem
                );


        // =====================================================
        // FIND SAME FOOD IN SAME ORDER
        // =====================================================

        List<OrderItem> sameItems =
                orderItemRepository
                        .findByOrderAndMenuId(
                                order,
                                currentItem.getMenu().getId()
                        );


        // =====================================================
        // FIND SERVED ITEMS
        // =====================================================

        List<OrderItem> servedItems =
                new ArrayList<>();


        for (OrderItem item : sameItems) {

            if (
                    item.getStatus()
                            == OrderItemStatus.SERVED
            ) {

                servedItems.add(
                        item
                );
            }
        }


        // =====================================================
        // NOTHING TO MERGE
        // =====================================================

        if (
                servedItems.size() <= 1
        ) {

            synchronizeOrderStatus(
                    order
            );


            return currentItem;
        }


        // =====================================================
        // SELECT MERGE TARGET
        // =====================================================

        OrderItem mergeTarget =
                servedItems.get(0);


        for (OrderItem item : servedItems) {

            if (
                    item.getId()
                            < mergeTarget.getId()
            ) {

                mergeTarget =
                        item;
            }
        }


        // =====================================================
        // MERGE QUANTITY AND PRICE
        // =====================================================

        int mergedQuantity =
                0;


        double mergedPrice =
                0.0;


        for (OrderItem item : servedItems) {

            mergedQuantity +=
                    item.getQuantity();


            if (
                    item.getPrice() != null
            ) {

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


        // =====================================================
        // SAVE MERGE TARGET
        // =====================================================

        mergeTarget =
                orderItemRepository.save(
                        mergeTarget
                );


        // =====================================================
        // DELETE OTHER SERVED DUPLICATES
        // =====================================================

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


        // =====================================================
        // SYNCHRONIZE PARENT ORDER
        // =====================================================

        synchronizeOrderStatus(
                order
        );


        return mergeTarget;
    }


    // =========================================================
    // KITCHEN - VALIDATE ITEM STATUS TRANSITION
    // =========================================================

    private void validateStatusTransition(
            OrderItemStatus currentStatus,
            OrderItemStatus newStatus
    ) {

        // =====================================================
        // NEW STATUS NULL
        // =====================================================

        if (
                newStatus == null
        ) {

            throw new IllegalArgumentException(
                    "New item status cannot be null"
            );
        }


        // =====================================================
        // CURRENT STATUS NULL
        // =====================================================

        if (
                currentStatus == null
        ) {

            if (
                    newStatus
                            != OrderItemStatus.ORDER_PLACED
            ) {

                throw new IllegalStateException(
                        "Invalid initial order item status"
                );
            }


            return;
        }


        // =====================================================
        // SAME STATUS
        // =====================================================

        if (
                currentStatus
                        == newStatus
        ) {

            return;
        }


        // =====================================================
        // ORDER_PLACED -> PREPARING
        // =====================================================

        if (
                currentStatus
                        == OrderItemStatus.ORDER_PLACED

                        &&

                        newStatus
                                == OrderItemStatus.PREPARING
        ) {

            return;
        }


        // =====================================================
        // PREPARING -> READY
        // =====================================================

        if (
                currentStatus
                        == OrderItemStatus.PREPARING

                        &&

                        newStatus
                                == OrderItemStatus.READY
        ) {

            return;
        }


        // =====================================================
        // READY -> SERVED
        // =====================================================

        if (
                currentStatus
                        == OrderItemStatus.READY

                        &&

                        newStatus
                                == OrderItemStatus.SERVED
        ) {

            return;
        }


        // =====================================================
        // INVALID TRANSITION
        // =====================================================

        throw new IllegalStateException(
                "Invalid status transition: "
                        + currentStatus
                        + " -> "
                        + newStatus
        );
    }


    // =========================================================
    // SYNCHRONIZE PARENT ORDER STATUS
    // =========================================================
    //
    // ORDER_PLACED -> PENDING
    // PREPARING    -> PREPARING
    // READY        -> READY
    // SERVED       -> SERVED
    //
    // IMPORTANT:
    // PAID and CANCELLED orders are never reopened.
    //
    // =========================================================

    private void synchronizeOrderStatus(
            Order order
    ) {

        // =====================================================
        // NEVER REOPEN PAID ORDER
        // =====================================================

        if (
                order.getStatus()
                        == OrderStatus.PAID
        ) {

            return;
        }


        // =====================================================
        // NEVER REOPEN CANCELLED ORDER
        // =====================================================

        if (
                order.getStatus()
                        == OrderStatus.CANCELLED
        ) {

            return;
        }


        // =====================================================
        // GET ALL ITEMS
        // =====================================================

        List<OrderItem> items =
                orderItemRepository.findByOrder(
                        order
                );


        // =====================================================
        // SAFETY CHECK
        // =====================================================

        if (
                items.isEmpty()
        ) {

            return;
        }


        // =====================================================
        // STATUS FLAGS
        // =====================================================

        boolean hasOrderPlaced =
                false;


        boolean hasPreparing =
                false;


        boolean hasReady =
                false;


        boolean hasServed =
                false;


        // =====================================================
        // CHECK ALL ITEMS
        // =====================================================

        for (OrderItem item : items) {

            OrderItemStatus status =
                    item.getStatus();


            // =================================================
            // NULL STATUS
            // =================================================

            if (
                    status == null
            ) {

                hasOrderPlaced =
                        true;

                continue;
            }


            // =================================================
            // ORDER PLACED
            // =================================================

            if (
                    status
                            == OrderItemStatus.ORDER_PLACED
            ) {

                hasOrderPlaced =
                        true;
            }


            // =================================================
            // PREPARING
            // =================================================

            else if (
                    status
                            == OrderItemStatus.PREPARING
            ) {

                hasPreparing =
                        true;
            }


            // =================================================
            // READY
            // =================================================

            else if (
                    status
                            == OrderItemStatus.READY
            ) {

                hasReady =
                        true;
            }


            // =================================================
            // SERVED
            // =================================================

            else if (
                    status
                            == OrderItemStatus.SERVED
            ) {

                hasServed =
                        true;
            }
        }


        // =====================================================
        // CALCULATE NEW ORDER STATUS
        // =====================================================

        OrderStatus newOrderStatus;


        if (
                hasOrderPlaced
        ) {

            newOrderStatus =
                    OrderStatus.PENDING;

        } else if (
                hasPreparing
        ) {

            newOrderStatus =
                    OrderStatus.PREPARING;

        } else if (
                hasReady
        ) {

            newOrderStatus =
                    OrderStatus.READY;

        } else if (
                hasServed
        ) {

            newOrderStatus =
                    OrderStatus.SERVED;

        } else {

            newOrderStatus =
                    OrderStatus.PENDING;
        }


        // =====================================================
        // UPDATE ORDER ONLY IF REQUIRED
        // =====================================================

        if (
                order.getStatus()
                        != newOrderStatus
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
    // BILL GENERATION
    // =========================================================
    //
    // ALL ITEMS ARE DISPLAYED.
    //
    // ONLY:
    //
    // READY
    // SERVED
    //
    // ARE INCLUDED IN SUBTOTAL.
    //
    // PREPARING + ORDER_PLACED:
    //
    // visible = YES
    // status = YES
    // amount included = NO
    //
    // =========================================================

    public BillResponse generateBill(
            Long orderId
    ) {

        // =====================================================
        // FIND ORDER
        // =====================================================

        Order order =
                orderRepository.findById(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order Not Found"
                                )
                        );


        // =====================================================
        // FIND ORDER ITEMS
        // =====================================================

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(
                        order
                );


        List<BillItemResponse> items =
                new ArrayList<>();


        double subtotal =
                0.0;


        // =====================================================
        // CREATE BILL ITEMS
        // =====================================================

        for (OrderItem item : orderItems) {

            // =================================================
            // SAFETY CHECK
            // =================================================

            if (
                    item.getQuantity() == null
                            ||
                            item.getQuantity() <= 0
            ) {

                continue;
            }


            if (
                    item.getPrice() == null
            ) {

                continue;
            }


            // =================================================
            // UNIT PRICE
            // =================================================

            double unitPrice =
                    item.getPrice()
                            / item.getQuantity();


            // =================================================
            // ITEM STATUS
            // =================================================

            String itemStatus;


            if (
                    item.getStatus() == null
            ) {

                itemStatus =
                        OrderItemStatus
                                .ORDER_PLACED
                                .name();

            } else {

                itemStatus =
                        item.getStatus()
                                .name();
            }


            // =================================================
            // ADD ITEM TO BILL RESPONSE
            // =================================================

            items.add(

                    new BillItemResponse(

                            item.getMenu()
                                    .getName(),

                            item.getQuantity(),

                            unitPrice,

                            item.getPrice(),

                            itemStatus

                    )
            );


            // =================================================
            // BILLING RULE
            // =================================================
            //
            // ONLY READY + SERVED ARE BILLABLE.
            //
            // =================================================

            if (
                    item.getStatus()
                            == OrderItemStatus.READY

                            ||

                            item.getStatus()
                                    == OrderItemStatus.SERVED
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
        // BILL RESPONSE
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
    // BILL DESK - SEARCH BILL BY ORDER ID
    // =========================================================

    public Order searchBillByOrderId(
            Long orderId
    ) {

        return orderRepository
                .findById(
                        orderId
                )
                .filter(
                        order ->
                                order.getStatus()
                                        == OrderStatus.PAID
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Paid Bill Not Found for Order ID: "
                                        + orderId
                        )
                );
    }


    // =========================================================
    // BILL DESK - SEARCH BILLS BY TABLE NUMBER
    // =========================================================

    public List<Order> searchBillsByTableNumber(
            Integer tableNumber
    ) {

        if (
                tableNumber == null
        ) {

            throw new IllegalArgumentException(
                    "Table number is required"
            );
        }


        return orderRepository
                .findAll()
                .stream()
                .filter(
                        order ->

                                order.getStatus()
                                        == OrderStatus.PAID

                                        &&

                                        order.getTableNumber()
                                                != null

                                        &&

                                        order.getTableNumber()
                                                .equals(
                                                        tableNumber
                                                )
                )
                .toList();
    }


    // =========================================================
    // BILL DESK - SEARCH BILLS BY DATE
    // =========================================================

    public List<Order> searchBillsByDate(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        // =====================================================
        // VALIDATE DATES
        // =====================================================

        if (
                startDateTime == null
                        ||
                        endDateTime == null
        ) {

            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }


        if (
                startDateTime.isAfter(
                        endDateTime
                )
        ) {

            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }


        // =====================================================
        // SEARCH PAID ORDERS
        // =====================================================

        return orderRepository
                .findAll()
                .stream()
                .filter(
                        order -> {

                            if (
                                    order.getStatus()
                                            != OrderStatus.PAID
                            ) {

                                return false;
                            }


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
}