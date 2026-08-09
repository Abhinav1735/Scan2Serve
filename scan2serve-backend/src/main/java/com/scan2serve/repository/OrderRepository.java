package com.scan2serve.repository;

import com.scan2serve.entity.Order;
import com.scan2serve.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long> {


    // =====================================================
    // FIND ORDERS BY STATUS
    // =====================================================

    List<Order> findByStatus(
            OrderStatus status
    );


    // =====================================================
    // FIND ACTIVE ORDER FOR TABLE
    // =====================================================
    //
    // PAID and CANCELLED orders are considered closed.
    //
    // Therefore, if Table 5 already has an active order,
    // the customer will continue using the same Order ID.
    //

    Optional<Order>
    findFirstByTableNumberAndStatusNotInOrderByIdDesc(
            Integer tableNumber,
            List<OrderStatus> statuses
    );

}