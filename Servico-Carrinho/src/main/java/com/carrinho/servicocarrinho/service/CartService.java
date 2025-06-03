package com.carrinho.servicocarrinho.service;

import com.carrinho.servicocarrinho.entity.Cart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {

    Cart createCart(Cart cart);
    List<Cart> getAllCart();
    Cart getCartByUsername(String username);
    boolean lockCart(String userId);
    Cart getCartByUserId(Long userId);
    boolean lockCartByUserId(Long userId);
    void unlockCartByUserId(Long userId);
}
