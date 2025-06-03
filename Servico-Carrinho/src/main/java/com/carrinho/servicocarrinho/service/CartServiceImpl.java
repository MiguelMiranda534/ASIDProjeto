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
    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getAllCart() {
        return cartRepository.findAll();
    }

    // antigo:
    @Override
    public Cart getCartByUsername(String username) {
        return cartRepository.findByUsername(username);
    }

    // === NOVOS métodos: ===
    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public void unlockCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null && cart.isLocked()) {
            cart.setLocked(false);
            cartRepository.save(cart);
        }
    }

    @Override
    public boolean lockCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        System.out.println("🔍 [CartService] Cart fetched para userId=" + userId + " → " + cart);
        if (cart == null) {
            System.out.println("   → NÃO encontrou Cart, vai devolver false");
            return false;
        }
        if (cart.isLocked()) {
            System.out.println("   → Já está locked=true, vai devolver false");
            return false;
        }
        cart.setLocked(true);
        cartRepository.save(cart);
        System.out.println("   → Lock aplicado com sucesso");
        return true;
    }

    // Mantendo compatibilidade, marcamos como @Deprecated:
    @Deprecated
    @Override
    public boolean lockCart(String userId) {
        // para não quebrar, ainda rodo pelo username:
        Cart cart = cartRepository.findByUsername(userId);
        if (cart == null) return false;
        if (cart.isLocked()) return false;
        cart.setLocked(true);
        cartRepository.save(cart);
        return true;
    }
}