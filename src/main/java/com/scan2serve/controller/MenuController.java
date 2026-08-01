package com.scan2serve.controller;

import com.scan2serve.entity.Menu;
import com.scan2serve.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    // Create Menu
    @PostMapping
    public Menu saveMenu(@RequestBody Menu menu) {
        return menuService.saveMenu(menu);
    }

    // Get All Menu
    @GetMapping
    public List<Menu> getAllMenus() {
        return menuService.getAllMenus();
    }

    // Get Menu By ID
    @GetMapping("/{id}")
    public Optional<Menu> getMenuById(@PathVariable Long id) {
        return menuService.getMenuById(id);
    }

    // Update Menu
    @PutMapping("/{id}")
    public Menu updateMenu(@PathVariable Long id,
                           @RequestBody Menu menu) {
        return menuService.updateMenu(id, menu);
    }

    // Delete Menu
    @DeleteMapping("/{id}")
    public String deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id);
    }

}