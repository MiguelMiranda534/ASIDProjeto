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
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService; // para criar Cart se não existir

    @Override
    public CartItem createCartItem(CartItem cartItem) {
        // 1) garantir que existe um Cart para este userId
        Long userId = cartItem.getUserId();
        if (cartService.getCartByUserId(userId) == null) {
            Cart novo = new Cart();
            novo.setUserId(userId);
            // (opcional) se quiser, pegue o username do CartItem ou defina como null
            novo.setUsername(cartItem.getUsername());
            novo.setCreatedDate(LocalDate.now());
            cartService.createCart(novo);
        }
        // 2) guardar o item
        return cartItemRepository.save(cartItem);
    }

    @Override
    public List<CartItem> getAllCartitem() {
        return cartItemRepository.findAll();
    }

    @Override
    public CartItem getCartItemById(Long id) {
        return cartItemRepository.findById(id).orElse(null);
    }

    @Override
    public CartItem patchCartQuantity(Long id, CartItem cartItem) {
        CartItem existItem = cartItemRepository.findById(id).orElse(null);
        if (existItem != null) {
            existItem.setQuantity(cartItem.getQuantity());
            cartItemRepository.save(existItem);
            return existItem;
        }
        return null;
    }

    @Override
    public CartItem patchCartSubTotal(Long id, CartItem cartItem) {
        CartItem existItem = cartItemRepository.findById(id).orElse(null);
        if (existItem != null) {
            existItem.setSubTotal(cartItem.getSubTotal());
            cartItemRepository.save(existItem);
            return existItem;
        }
        return null;
    }

    @Override
    public CartItem deleteCartItyItemById(Long id) {
        CartItem existItem = cartItemRepository.findById(id).orElse(null);
        if (existItem != null) {
            cartItemRepository.delete(existItem);
        }
        return null;
    }

    @Override
    public void clearCart() {
        cartItemRepository.deleteAll();
    }

    @Override
    public void resetAutoIncrement() {
        cartItemRepository.resetAutoIncrement();
    }

    // == métodos antigos por username (marcar como @Deprecated) ==
    @Deprecated
    @Override
    public List<CartItem> getCartItemsByUsername(String username) {
        return cartItemRepository.findByUsername(username);
    }

    @Deprecated
    @Override
    @Transactional
    public void clearCartForUser(String username) {
        cartItemRepository.deleteByUsername(username);
    }

    // == NOVOS métodos usando userId: ==
    @Override
    public List<CartItem> getCartItemsByUserId(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void clearCartForUserId(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}