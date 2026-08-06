package com.scan2serve.repository;

import com.scan2serve.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    boolean existsByTableNumber(Integer tableNumber);

}