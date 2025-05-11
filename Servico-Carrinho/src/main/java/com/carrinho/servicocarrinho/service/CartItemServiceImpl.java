package com.carrinho.servicocarrinho.service;

import com.carrinho.servicocarrinho.entity.Cart;
import com.carrinho.servicocarrinho.entity.CartItem;
import com.carrinho.servicocarrinho.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService{

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;              // <<< para criar cart se não existir


    @Override
    public CartItem createCartItem(CartItem cartItem) {
        // 1) garantir que existe um Cart para este username
        String username = cartItem.getUsername();
        if (cartService.getCartByUsername(username) == null) {
            Cart novo = new Cart();
            novo.setUsername(username);
            novo.setCreatedDate(LocalDate.now());
            cartService.createCart(novo);
        }
        // 2) guardar o item
        return cartItemRepository.save(cartItem);
    }


    public List<CartItem> getAllCartitem(){

        return cartItemRepository.findAll();


    }

    public CartItem getCartItemById(Long id){

        return cartItemRepository.findById(id).orElse(null);
    }

    public CartItem patchCartQuantity(Long id, CartItem cartItem){
        CartItem existItem = cartItemRepository.findById(id).orElse(null);
    
        if (existItem != null) {
            
            existItem.setQuantity(cartItem.getQuantity());
            cartItemRepository.save(existItem);
    
            return existItem;
        } else { 
            return null;
        }
    }
    

    public CartItem patchCartSubTotal(Long id, CartItem cartItem){
        CartItem existItem = cartItemRepository.findById(id).orElse(null);
    
        if (existItem != null) {
            
            existItem.setSubTotal(cartItem.getSubTotal());
            cartItemRepository.save(existItem);
    
            return existItem;
        } else { 
            return null;
        }
    }


    public CartItem deleteCartItyItemById(Long id){

        CartItem existItem = cartItemRepository.findById(id).orElse(null);

        if(existItem !=null){

            cartItemRepository.delete(existItem);
        }
        return null;
    }


    public void clearCart(){

        cartItemRepository.deleteAll();
        
    }

    public void resetAutoIncrement() {
        cartItemRepository.resetAutoIncrement();
    }

    public List<CartItem> getCartItemsByUsername(String username) {

        return cartItemRepository.findByUsername(username);
        
    }

    @Override
    @Transactional
    public void clearCartForUser(String username) {
        cartItemRepository.deleteByUsername(username);
    }
}
