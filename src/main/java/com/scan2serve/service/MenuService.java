package com.scan2serve.service;

import com.scan2serve.entity.Menu;
import com.scan2serve.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    // Create Menu Item
    public Menu saveMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    // Get All Menu Items
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    // Get Menu By ID
    public Optional<Menu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }

    // Update Menu
    public Menu updateMenu(Long id, Menu updatedMenu) {

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        menu.setName(updatedMenu.getName());
        menu.setDescription(updatedMenu.getDescription());
        menu.setPrice(updatedMenu.getPrice());
        menu.setAvailable(updatedMenu.getAvailable());

        return menuRepository.save(menu);
    }

    // Delete Menu
    public String deleteMenu(Long id) {

        menuRepository.deleteById(id);

        return "Menu Deleted Successfully";
    }

}