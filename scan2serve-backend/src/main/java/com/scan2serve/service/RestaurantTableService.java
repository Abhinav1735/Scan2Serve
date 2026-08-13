package com.scan2serve.service;

import com.scan2serve.dto.RestaurantTableRequest;
import com.scan2serve.entity.RestaurantTable;
import com.scan2serve.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantTableService {

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    // Create Table
    public RestaurantTable createTable(RestaurantTableRequest request) {

        if (restaurantTableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new RuntimeException("Table Number already exists");
        }

        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(request.getTableNumber());
        table.setActive(request.getActive());

        return restaurantTableRepository.save(table);
    }

    // Get All Tables
    public List<RestaurantTable> getAllTables() {
        return restaurantTableRepository.findAll();
    }

    // Get Table By Id
    public RestaurantTable getTableById(Long id) {

        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Not Found"));
    }

    // Update Table
    public RestaurantTable updateTable(Long id, RestaurantTableRequest request) {

        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Not Found"));

        // Only check for duplicates if the table number is being changed
        if (!table.getTableNumber().equals(request.getTableNumber())
                && restaurantTableRepository.existsByTableNumber(request.getTableNumber())) {

            throw new RuntimeException("Table Number already exists");
        }

        table.setTableNumber(request.getTableNumber());
        table.setActive(request.getActive());

        return restaurantTableRepository.save(table);
    }

    // Delete Table
    public String deleteTable(Long id) {

        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Not Found"));

        restaurantTableRepository.delete(table);

        return "Table Deleted Successfully";
    }
}