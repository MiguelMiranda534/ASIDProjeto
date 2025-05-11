package com.carrinho.servicocarrinho.service;

import com.carrinho.servicocarrinho.entity.Cart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {

    Cart createCart(Cart cart);
    List<Cart> getAllCart();
    Cart getCartByUsername(String username);
}
