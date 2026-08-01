package com.scan2serve.controller;

import com.scan2serve.dto.CartRequest;
import com.scan2serve.entity.Cart;
import com.scan2serve.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Add Item
    @PostMapping
    public Cart addToCart(@RequestBody CartRequest request) {
        return cartService.addToCart(request);
    }

    // View Cart of a Table
    @GetMapping("/{tableNumber}")
    public List<Cart> getCart(@PathVariable Integer tableNumber) {
        return cartService.getCart(tableNumber);
    }

    // Remove Item
    @DeleteMapping("/{id}")
    public String removeItem(@PathVariable Long id) {
        return cartService.removeItem(id);
    }

}