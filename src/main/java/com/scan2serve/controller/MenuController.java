package com.scan2serve.controller;

import com.scan2serve.dto.MenuRequest;
import com.scan2serve.entity.Menu;
import com.scan2serve.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    // Create Menu
    @PostMapping
    public Menu saveMenu(@RequestBody MenuRequest request) {
        return menuService.saveMenu(request);
    }

    // Get All Menu
    @GetMapping
    public List<Menu> getAllMenus() {
        return menuService.getAllMenus();
    }

    // Get Menu By Id
    @GetMapping("/{id}")
    public Menu getMenuById(@PathVariable Long id) {
        return menuService.getMenuById(id);
    }

    // Update Menu
    @PutMapping("/{id}")
    public Menu updateMenu(@PathVariable Long id,
                           @RequestBody MenuRequest request) {

        return menuService.updateMenu(id, request);
    }

    // Delete Menu
    @DeleteMapping("/{id}")
    public String deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id);
    }
}