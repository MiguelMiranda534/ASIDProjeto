package com.ijse.bookstore.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.Cart;
import com.ijse.bookstore.repository.CartRepository;


@Service
public class CartServiceImpl implements CartService{
    
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
    public Cart getCartIdByUserId(Long userId){

        return cartRepository.getCartIdByUserId(userId);
    }
}
