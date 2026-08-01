package com.scan2serve.service;

import com.scan2serve.dto.CartRequest;
import com.scan2serve.entity.Cart;
import com.scan2serve.entity.Menu;
import com.scan2serve.repository.CartRepository;
import com.scan2serve.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuRepository menuRepository;

    // Add Item to Cart
    public Cart addToCart(CartRequest request) {

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new RuntimeException("Menu Not Found"));

        Cart cart = new Cart();

        cart.setTableNumber(request.getTableNumber());
        cart.setMenu(menu);
        cart.setQuantity(request.getQuantity());

        return cartRepository.save(cart);
    }

    // Get Cart of a Table
    public List<Cart> getCart(Integer tableNumber) {
        return cartRepository.findByTableNumber(tableNumber);
    }

    // Remove Item from Cart
    public String removeItem(Long id) {

        cartRepository.deleteById(id);

        return "Item Removed Successfully";
    }

}