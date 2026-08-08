package com.scan2serve.repository;

import com.scan2serve.entity.Order;
import com.scan2serve.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    // Find the latest active/unpaid order for a table
    Optional<Order> findFirstByTableNumberAndStatusNotOrderByIdDesc(
            Integer tableNumber,
            OrderStatus status);
}