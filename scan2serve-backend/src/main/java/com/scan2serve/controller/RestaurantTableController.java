package com.scan2serve.controller;

import com.scan2serve.dto.RestaurantTableRequest;
import com.scan2serve.entity.RestaurantTable;
import com.scan2serve.response.ApiResponse;
import com.scan2serve.service.RestaurantTableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/tables")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService restaurantTableService;

    // ============================
    // Create Table
    // ============================

    @PostMapping
    public ApiResponse<RestaurantTable> createTable(
            @Valid @RequestBody RestaurantTableRequest request) {

        RestaurantTable table = restaurantTableService.createTable(request);

        return new ApiResponse<>(
                true,
                "Table Created Successfully",
                table
        );
    }

    // ============================
    // Get All Tables
    // ============================

    @GetMapping
    public ApiResponse<List<RestaurantTable>> getAllTables() {

        return new ApiResponse<>(
                true,
                "Tables Fetched Successfully",
                restaurantTableService.getAllTables()
        );
    }

    // ============================
    // Get Table By Id
    // ============================

    @GetMapping("/{id}")
    public ApiResponse<RestaurantTable> getTableById(
            @PathVariable Long id) {

        return new ApiResponse<>(
                true,
                "Table Found",
                restaurantTableService.getTableById(id)
        );
    }

    // ============================
    // Update Table
    // ============================

    @PutMapping("/{id}")
    public ApiResponse<RestaurantTable> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantTableRequest request) {

        RestaurantTable table =
                restaurantTableService.updateTable(id, request);

        return new ApiResponse<>(
                true,
                "Table Updated Successfully",
                table
        );
    }

    // ============================
    // Delete Table
    // ============================

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTable(
            @PathVariable Long id) {

        String message = restaurantTableService.deleteTable(id);

        return new ApiResponse<>(
                true,
                message,
                null
        );
    }
}