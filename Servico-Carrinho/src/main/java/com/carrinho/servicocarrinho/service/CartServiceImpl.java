package com.carrinho.servicocarrinho.service;

import com.carrinho.servicocarrinho.entity.Cart;
import com.carrinho.servicocarrinho.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartRepository cartRepository;

    @Override
    public Cart createCart(Cart cart){

        return cartRepository.save(cart);
    }


    @Override
    public List<Cart> getAllCart(){

        return cartRepository.findAll();
    }

    @Override
    public Cart getCartByUsername(String username){

        return cartRepository.findByUsername(username);
    }
}
