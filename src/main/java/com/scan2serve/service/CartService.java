package com.scan2serve.service;

import com.scan2serve.dto.CartRequest;
import com.scan2serve.entity.Cart;
import com.scan2serve.entity.Menu;
import com.scan2serve.repository.CartRepository;
import com.scan2serve.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuRepository menuRepository;

    // ==========================
    // Add Item To Cart
    // ==========================

    public Cart addToCart(CartRequest request) {

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new RuntimeException("Menu Not Found"));

        Cart cart = new Cart();
        cart.setTableNumber(request.getTableNumber());
        cart.setMenu(menu);
        cart.setQuantity(request.getQuantity());

        return cartRepository.save(cart);
    }

    // ==========================
    // Get Cart
    // ==========================

    public List<Cart> getCart(Integer tableNumber) {

        return cartRepository.findByTableNumber(tableNumber);
    }

    // ==========================
    // Remove Single Cart Item
    // ==========================

    @Transactional
    public String removeItem(Long id) {

        try {

            if (!cartRepository.existsById(id)) {
                throw new RuntimeException("Cart Item Not Found");
            }

            cartRepository.deleteById(id);

            return "Item Removed Successfully";

        } catch (EmptyResultDataAccessException e) {

            throw new RuntimeException("Cart Item Not Found");
        }
    }

    // ==========================
    // Clear Complete Cart
    // ==========================

    @Transactional
    public String clearCart(Integer tableNumber) {

        List<Cart> cartItems = cartRepository.findByTableNumber(tableNumber);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is Empty");
        }

        cartRepository.deleteByTableNumber(tableNumber);

        return "Cart Cleared Successfully";
    }
}