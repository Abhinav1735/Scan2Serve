package com.scan2serve.repository;

import com.scan2serve.entity.Order;
import com.scan2serve.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    // FIND ORDERS BY MULTIPLE STATUSES
    // =====================================================

    List<Order> findByStatusIn(
            List<OrderStatus> statuses
    );


    // =====================================================
    // FIND ACTIVE ORDER FOR TABLE
    // =====================================================

    Optional<Order>
    findFirstByTableNumberAndStatusNotInOrderByIdDesc(
            Integer tableNumber,
            List<OrderStatus> statuses
    );


    // =====================================================
    // BILL DESK - SEARCH PAID BILLS BY ORDER ID
    // =====================================================

    Optional<Order> findByIdAndStatus(
            Long id,
            OrderStatus status
    );


    // =====================================================
    // BILL DESK - SEARCH PAID BILLS BY TABLE NUMBER
    // =====================================================

    List<Order>
    findByTableNumberAndStatusOrderByOrderTimeDesc(
            Integer tableNumber,
            OrderStatus status
    );


    // =====================================================
    // BILL DESK - SEARCH PAID BILLS BY DATE RANGE
    // =====================================================

    List<Order>
    findByStatusAndOrderTimeGreaterThanEqualAndOrderTimeLessThan(
            OrderStatus status,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}