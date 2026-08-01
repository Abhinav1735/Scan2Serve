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
    private RestaurantTableRepository repository;

    // Create Table
    public RestaurantTable createTable(RestaurantTableRequest request){

        RestaurantTable table = new RestaurantTable();

        table.setTableNumber(request.getTableNumber());
        table.setActive(request.getActive());

        return repository.save(table);
    }

    // Get All Tables
    public List<RestaurantTable> getAllTables(){
        return repository.findAll();
    }

    // Get Table By Id
    public RestaurantTable getTableById(Long id){

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Not Found"));
    }

    // Update Table
    public RestaurantTable updateTable(Long id, RestaurantTableRequest request){

        RestaurantTable table = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Not Found"));

        table.setTableNumber(request.getTableNumber());
        table.setActive(request.getActive());

        return repository.save(table);
    }

    // Delete Table
    public String deleteTable(Long id){

        repository.deleteById(id);

        return "Table Deleted Successfully";
    }

}