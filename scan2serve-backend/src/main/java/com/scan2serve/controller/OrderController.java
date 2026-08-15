package com.scan2serve.controller;

import com.scan2serve.dto.BillResponse;
import com.scan2serve.dto.KitchenOrderResponse;
import com.scan2serve.dto.OrderItemStatusRequest;
import com.scan2serve.dto.OrderRequest;
import com.scan2serve.dto.OrderStatusRequest;
import com.scan2serve.dto.PaymentRequest;
import com.scan2serve.dto.PaymentResponse;

import com.scan2serve.entity.Order;
import com.scan2serve.entity.OrderItem;

import com.scan2serve.enums.OrderStatus;

import com.scan2serve.response.ApiResponse;

import com.scan2serve.service.OrderService;
import com.scan2serve.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@RestController
public class OrderController {


    @Autowired
    private OrderService orderService;


    @Autowired
    private PaymentService paymentService;


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

        /*
         * CUSTOMER BILL
         *
         * This returns ALL order items with their
         * current kitchen status.
         *
         * ORDER_PLACED -> shown
         * PREPARING    -> shown
         * READY        -> shown
         * SERVED       -> shown
         *
         * Only READY and SERVED are counted in subtotal.
         */
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
    public ApiResponse<List<KitchenOrderResponse>>
    getKitchenOrders() {

        return new ApiResponse<>(
                true,
                "Kitchen Orders Fetched Successfully",
                orderService.getKitchenOrders()
        );
    }


    // =========================================================
    // KITCHEN - UPDATE INDIVIDUAL ITEM STATUS
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


    // =========================================================
    // BILL DESK APIs
    // =========================================================


    // =========================================================
    // BILL DESK - GET ACTIVE ORDERS
    // =========================================================

    @GetMapping("/bill-desk/orders")
    public ApiResponse<List<Order>> getBillDeskOrders() {

        return new ApiResponse<>(
                true,
                "Active Orders Fetched Successfully",
                orderService.getBillDeskOrders()
        );
    }


    // =========================================================
    // BILL DESK - GET BILL
    // =========================================================

    @GetMapping(
            "/bill-desk/orders/{orderId}/bill"
    )
    public ApiResponse<BillResponse> getBillDeskBill(
            @PathVariable Long orderId
    ) {

        /*
         * BILL DESK BILL
         *
         * Only READY and SERVED items are returned.
         */
        BillResponse bill =
                orderService.generateBillForBillDesk(
                        orderId
                );


        return new ApiResponse<>(
                true,
                "Bill Found",
                bill
        );
    }


    // =========================================================
    // BILL DESK - GET BILL BY ORDER ID
    // =========================================================

    @GetMapping(
            "/bill-desk/orders/{orderId}"
    )
    public ApiResponse<BillResponse> getBillDeskOrder(
            @PathVariable Long orderId
    ) {

        /*
         * This endpoint is also used by the Bill Desk frontend.
         *
         * IMPORTANT:
         * Do NOT call generateBill() here because that is
         * the customer bill.
         */
        BillResponse bill =
                orderService.generateBillForBillDesk(
                        orderId
                );


        return new ApiResponse<>(
                true,
                "Bill Found",
                bill
        );
    }


    // =========================================================
    // BILL DESK - PROCESS PAYMENT
    // =========================================================

    @PostMapping(
            "/bill-desk/orders/{orderId}/payment"
    )
    public ApiResponse<PaymentResponse> processPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentRequest request
    ) {

        PaymentResponse payment =
                paymentService.processPayment(
                        orderId,
                        request.getPaymentMethod()
                );


        return new ApiResponse<>(
                true,
                "Payment Completed Successfully",
                payment
        );
    }


    // =========================================================
    // BILL DESK - GET PAYMENT
    // =========================================================

    @GetMapping(
            "/bill-desk/orders/{orderId}/payment"
    )
    public ApiResponse<PaymentResponse> getPayment(
            @PathVariable Long orderId
    ) {

        PaymentResponse payment =
                paymentService.getPaymentByOrderId(
                        orderId
                );


        return new ApiResponse<>(
                true,
                "Payment Found",
                payment
        );
    }


    // =========================================================
    // BILL DESK - GET OLD BILLS
    // =========================================================

    @GetMapping(
            "/bill-desk/old-bills"
    )
    public ApiResponse<List<Order>> getOldBills(

            @RequestParam(
                    required = false
            )
            Long orderId,

            @RequestParam(
                    required = false
            )
            Integer tableNumber,

            @RequestParam(
                    required = false
            )
            String date

    ) {

        LocalDate selectedDate =
                null;


        if (
                date != null
                        &&
                        !date.isBlank()
        ) {

            selectedDate =
                    LocalDate.parse(
                            date
                    );
        }


        List<Order> oldBills =
                orderService.searchOldBills(
                        orderId,
                        tableNumber,
                        selectedDate
                );


        return new ApiResponse<>(
                true,
                "Old Bills Fetched Successfully",
                oldBills
        );
    }


    // =========================================================
    // BILL DESK - SEARCH BY ORDER ID
    // =========================================================

    @GetMapping(
            "/bill-desk/search/order/{orderId}"
    )
    public ApiResponse<Order> searchBillByOrderId(
            @PathVariable Long orderId
    ) {

        return new ApiResponse<>(
                true,
                "Bill Found",
                orderService.searchBillByOrderId(
                        orderId
                )
        );
    }


    // =========================================================
    // BILL DESK - SEARCH BY TABLE NUMBER
    // =========================================================

    @GetMapping(
            "/bill-desk/search/table/{tableNumber}"
    )
    public ApiResponse<List<Order>> searchBillsByTableNumber(
            @PathVariable Integer tableNumber
    ) {

        return new ApiResponse<>(
                true,
                "Bills Found",
                orderService.searchBillsByTableNumber(
                        tableNumber
                )
        );
    }


    // =========================================================
    // BILL DESK - SEARCH BY DATE
    // =========================================================

    @GetMapping(
            "/bill-desk/search/date/{date}"
    )
    public ApiResponse<List<Order>> searchBillsByDate(
            @PathVariable String date
    ) {

        LocalDate selectedDate =
                LocalDate.parse(
                        date
                );


        LocalDateTime startDateTime =
                selectedDate.atStartOfDay();


        LocalDateTime endDateTime =
                selectedDate
                        .plusDays(1)
                        .atStartOfDay();


        return new ApiResponse<>(
                true,
                "Bills Found",
                orderService.searchBillsByDate(
                        startDateTime,
                        endDateTime
                )
        );
    }
}