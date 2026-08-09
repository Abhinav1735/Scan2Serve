package com.scan2serve.service;

import com.scan2serve.dto.BillItemResponse;
import com.scan2serve.dto.BillResponse;
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

        if (
                cartItems.isEmpty()
        ) {

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

        if (
                order == null
        ) {

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

        for (
                Cart cart :
                cartItems
        ) {


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
        // RECALCULATE COMPLETE BILL TOTAL
        // =====================================================

        List<OrderItem> allItems =
                orderItemRepository.findByOrder(
                        order
                );


        double total =
                0.0;


        for (
                OrderItem item :
                allItems
        ) {

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
    // KITCHEN - GET PENDING ORDERS
    // =========================================================

    public List<Order> getKitchenOrders() {

        return orderRepository.findByStatus(
                OrderStatus.PENDING
        );
    }


    // =========================================================
    // KITCHEN - UPDATE INDIVIDUAL ITEM STATUS
    // =========================================================
    //
    // This is the ONLY updateItemStatus() method.
    //
    // ORDER_PLACED -> PREPARING -> READY -> SERVED
    //
    // If an item becomes SERVED, same-food SERVED batches
    // in the same order are merged.
    //

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
        // UPDATE STATUS
        // =====================================================

        currentItem.setStatus(
                newStatus
        );


        // =====================================================
        // IF NOT SERVED
        // =====================================================
        //
        // Keep the batch separate while it is being prepared.
        //

        if (
                newStatus != OrderItemStatus.SERVED
        ) {

            return orderItemRepository.save(
                    currentItem
            );
        }


        // =====================================================
        // SAVE CURRENT ITEM AS SERVED
        // =====================================================

        currentItem =
                orderItemRepository.save(
                        currentItem
                );


        // =====================================================
        // FIND ALL SAME FOOD IN SAME ORDER
        // =====================================================

        List<OrderItem> sameItems =
                orderItemRepository
                        .findByOrderAndMenuId(
                                currentItem.getOrder(),
                                currentItem.getMenu().getId()
                        );


        // =====================================================
        // FIND SERVED ITEMS
        // =====================================================

        List<OrderItem> servedItems =
                new ArrayList<>();


        for (
                OrderItem item :
                sameItems
        ) {

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

            return currentItem;
        }


        // =====================================================
        // SELECT MERGE TARGET
        // =====================================================
        //
        // Keep the oldest served row.
        //

        OrderItem mergeTarget =
                servedItems.get(0);


        for (
                OrderItem item :
                servedItems
        ) {

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


        for (
                OrderItem item :
                servedItems
        ) {

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
        // DELETE OTHER SERVED DUPLICATE BATCHES
        // =====================================================

        for (
                OrderItem item :
                servedItems
        ) {

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
        // RETURN MERGED ITEM
        // =====================================================

        return mergeTarget;
    }


    // =========================================================
    // BILL GENERATION
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

        for (
                OrderItem item :
                orderItems
        ) {


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
            // ADD BILL ITEM
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
            // SUBTOTAL
            // =================================================

            subtotal +=
                    item.getPrice();
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

}