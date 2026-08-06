package com.scan2serve.controller;

import com.scan2serve.dto.CategoryRequest;
import com.scan2serve.entity.Category;
import com.scan2serve.response.ApiResponse;
import com.scan2serve.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Create Category
    @PostMapping
    public ApiResponse<Category> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        Category category = categoryService.createCategory(request);

        return new ApiResponse<>(
                true,
                "Category Created Successfully",
                category
        );
    }

    // Get All Categories
    @GetMapping
    public ApiResponse<List<Category>> getAllCategories() {

        return new ApiResponse<>(
                true,
                "Categories Fetched Successfully",
                categoryService.getAllCategories()
        );
    }

    // Get Category By Id
    @GetMapping("/{id}")
    public ApiResponse<Category> getCategoryById(
            @PathVariable Long id) {

        return new ApiResponse<>(
                true,
                "Category Found",
                categoryService.getCategoryById(id)
        );
    }

    // Update Category
    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        Category category = categoryService.updateCategory(id, request);

        return new ApiResponse<>(
                true,
                "Category Updated Successfully",
                category
        );
    }

    // Delete Category
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(
            @PathVariable Long id) {

        String message = categoryService.deleteCategory(id);

        return new ApiResponse<>(
                true,
                message,
                null
        );
    }
}