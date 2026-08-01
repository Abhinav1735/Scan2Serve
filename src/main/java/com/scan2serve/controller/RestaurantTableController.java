package com.scan2serve.controller;

import com.scan2serve.dto.RestaurantTableRequest;
import com.scan2serve.entity.RestaurantTable;
import com.scan2serve.service.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService service;

    @PostMapping
    public RestaurantTable createTable(@RequestBody RestaurantTableRequest request){
        return service.createTable(request);
    }

    @GetMapping
    public List<RestaurantTable> getAllTables(){
        return service.getAllTables();
    }

    @GetMapping("/{id}")
    public RestaurantTable getTableById(@PathVariable Long id){
        return service.getTableById(id);
    }

    @PutMapping("/{id}")
    public RestaurantTable updateTable(@PathVariable Long id,
                                       @RequestBody RestaurantTableRequest request){

        return service.updateTable(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteTable(@PathVariable Long id){

        return service.deleteTable(id);
    }

}