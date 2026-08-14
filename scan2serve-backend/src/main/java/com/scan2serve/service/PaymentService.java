package com.scan2serve.service;

import com.scan2serve.dto.BillResponse;
import com.scan2serve.dto.PaymentResponse;

import com.scan2serve.entity.Order;
import com.scan2serve.entity.OrderItem;
import com.scan2serve.entity.Payment;

import com.scan2serve.enums.OrderItemStatus;
import com.scan2serve.enums.PaymentMethod;
import com.scan2serve.enums.PaymentStatus;

import com.scan2serve.exception.custom.PaymentNotFoundException;

import com.scan2serve.repository.OrderItemRepository;
import com.scan2serve.repository.OrderRepository;
import com.scan2serve.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class PaymentService {


    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private PaymentRepository paymentRepository;


    @Autowired
    private OrderItemRepository orderItemRepository;


    @Autowired
    private OrderService orderService;


    // =========================================================
    // BILL DESK - PROCESS PAYMENT
    // =========================================================
    //
    // PAYMENT RULE:
    //
    // Payment is allowed when at least one order item is:
    //
    // READY
    // OR
    // SERVED
    //
    // READY + SERVED
    //       ↓
    //   BILLABLE
    //
    // PREPARING + ORDER_PLACED
    //       ↓
    // NOT BILLABLE
    //
    // After payment:
    //
    // ENTIRE ORDER → PAID
    //
    // Unfinished items remain attached to the old order.
    // They are NOT moved to a new order.
    //
    // =========================================================

    public PaymentResponse processPayment(
            Long orderId,
            PaymentMethod paymentMethod
    ) {

        // =====================================================
        // VALIDATE PAYMENT METHOD
        // =====================================================

        if (
                paymentMethod == null
        ) {

            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }


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
        // CHECK IF ORDER IS ALREADY PAID
        // =====================================================

        if (
                order.getStatus()
                        == com.scan2serve.enums.OrderStatus.PAID
        ) {

            throw new IllegalStateException(
                    "Order is already paid"
            );
        }


        // =====================================================
        // CHECK IF ORDER IS CANCELLED
        // =====================================================

        if (
                order.getStatus()
                        == com.scan2serve.enums.OrderStatus.CANCELLED
        ) {

            throw new IllegalStateException(
                    "Payment is not allowed for a cancelled order"
            );
        }


        // =====================================================
        // GET ALL ORDER ITEMS
        // =====================================================

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(
                        order
                );


        // =====================================================
        // CHECK BILLABLE ITEMS
        // =====================================================
        //
        // At least ONE item must be READY or SERVED.
        //
        // =====================================================

        boolean hasBillableItem =
                false;


        for (
                OrderItem item : orderItems
        ) {

            if (
                    item.getStatus()
                            == OrderItemStatus.READY

                            ||

                            item.getStatus()
                                    == OrderItemStatus.SERVED
            ) {

                hasBillableItem =
                        true;

                break;
            }
        }


        // =====================================================
        // NO BILLABLE ITEM
        // =====================================================

        if (
                !hasBillableItem
        ) {

            throw new IllegalStateException(
                    "Payment is allowed only when at least one item is READY or SERVED"
            );
        }


        // =====================================================
        // CHECK EXISTING PAYMENT
        // =====================================================

        if (
                paymentRepository.existsByOrder(
                        order
                )
        ) {

            throw new IllegalStateException(
                    "Payment already exists for this order"
            );
        }


        // =====================================================
        // GET FINAL BILL
        // =====================================================
        //
        // generateBill() already applies the rule:
        //
        // READY  → included
        // SERVED → included
        //
        // PREPARING    → excluded
        // ORDER_PLACED → excluded
        //
        // =====================================================

        BillResponse bill =
                orderService.generateBill(
                        orderId
                );


        // =====================================================
        // SAFETY CHECK
        // =====================================================

        if (
                bill == null
        ) {

            throw new IllegalStateException(
                    "Unable to generate bill"
            );
        }


        // =====================================================
        // SAFETY CHECK FOR GRAND TOTAL
        // =====================================================

        if (
                bill.getGrandTotal() <= 0
        ) {

            throw new IllegalStateException(
                    "Payment amount must be greater than zero"
            );
        }


        // =====================================================
        // CREATE PAYMENT
        // =====================================================

        Payment payment =
                new Payment();


        // =====================================================
        // LINK PAYMENT TO ORDER
        // =====================================================

        payment.setOrder(
                order
        );


        // =====================================================
        // SET FINAL BILL AMOUNT
        // =====================================================

        payment.setAmount(
                bill.getGrandTotal()
        );


        // =====================================================
        // PAYMENT METHOD
        // =====================================================

        payment.setPaymentMethod(
                paymentMethod
        );


        // =====================================================
        // PAYMENT STATUS
        // =====================================================

        payment.setPaymentStatus(
                PaymentStatus.COMPLETED
        );


        // =====================================================
        // PAYMENT TIME
        // =====================================================

        payment.setPaymentTime(
                LocalDateTime.now()
        );


        // =====================================================
        // SAVE PAYMENT
        // =====================================================

        payment =
                paymentRepository.save(
                        payment
                );


        // =====================================================
        // MARK ENTIRE ORDER AS PAID
        // =====================================================
        //
        // IMPORTANT:
        //
        // We DO NOT change the unfinished items.
        //
        // PREPARING stays PREPARING.
        // ORDER_PLACED stays ORDER_PLACED.
        //
        // They remain attached to this historical PAID order.
        //
        // They will NOT be copied into the next order.
        //
        // =====================================================

        order.setStatus(
                com.scan2serve.enums.OrderStatus.PAID
        );


        // =====================================================
        // SAVE CLOSED ORDER
        // =====================================================

        order =
                orderRepository.save(
                        order
                );


        // =====================================================
        // RETURN PAYMENT RESPONSE
        // =====================================================

        return convertToResponse(
                payment
        );
    }


    // =========================================================
    // BILL DESK - GET PAYMENT
    // =========================================================

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(
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
        // FIND PAYMENT
        // =====================================================

        Payment payment =
                paymentRepository
                        .findByOrder(
                                order
                        )
                        .orElseThrow(
                                () -> new PaymentNotFoundException(
                                        "Payment Not Found"
                                )
                        );


        // =====================================================
        // RETURN PAYMENT RESPONSE
        // =====================================================

        return convertToResponse(
                payment
        );
    }


    // =========================================================
    // CONVERT PAYMENT ENTITY → PAYMENT RESPONSE
    // =========================================================

    private PaymentResponse convertToResponse(
            Payment payment
    ) {

        return new PaymentResponse(

                payment.getId(),

                payment.getOrder().getId(),

                payment.getAmount(),

                payment.getPaymentMethod(),

                payment.getPaymentStatus(),

                payment.getPaymentTime()

        );
    }
}