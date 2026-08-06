package com.scan2serve.service;

import com.scan2serve.dto.CategoryRequest;
import com.scan2serve.entity.Category;
import com.scan2serve.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Create Category
    public Category createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDisplayOrder(request.getDisplayOrder());

        return categoryRepository.save(category);
    }

    // Get All Categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    // Get Category By Id
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category Not Found"));
    }

    // Update Category
    public Category updateCategory(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category Not Found"));

        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByName(request.getName())) {

            throw new RuntimeException("Category already exists");
        }

        category.setName(request.getName());
        category.setDisplayOrder(request.getDisplayOrder());

        return categoryRepository.save(category);
    }

    // Delete Category
    public String deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category Not Found"));

        categoryRepository.delete(category);

        return "Category Deleted Successfully";
    }
}