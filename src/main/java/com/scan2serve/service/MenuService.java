package com.scan2serve.service;

import com.scan2serve.dto.MenuRequest;
import com.scan2serve.entity.Category;
import com.scan2serve.entity.Menu;
import com.scan2serve.repository.CategoryRepository;
import com.scan2serve.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Create Menu
    public Menu saveMenu(MenuRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category Not Found"));

        Menu menu = new Menu();

        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setPrice(request.getPrice());
        menu.setAvailable(request.getAvailable());
        menu.setCategory(category);

        return menuRepository.save(menu);
    }

    // Get All Menu
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    // Get Menu By Id
    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu Not Found"));
    }

    // Update Menu
    public Menu updateMenu(Long id, MenuRequest request) {

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu Not Found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category Not Found"));

        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setPrice(request.getPrice());
        menu.setAvailable(request.getAvailable());
        menu.setCategory(category);

        return menuRepository.save(menu);
    }

    // Delete Menu
    public String deleteMenu(Long id) {

        menuRepository.deleteById(id);

        return "Menu Deleted Successfully";
    }
}