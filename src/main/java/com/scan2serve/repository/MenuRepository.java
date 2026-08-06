package com.scan2serve.repository;

import com.scan2serve.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // Check duplicate menu name
    boolean existsByName(String name);

    // Get only available menu items (used by CustomerService)
    List<Menu> findByAvailableTrue();

}