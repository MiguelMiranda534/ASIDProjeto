package com.carrinho.servicocarrinho.controller;

import com.carrinho.servicocarrinho.entity.Cart;
import com.carrinho.servicocarrinho.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    @Autowired
    private CartService cartService;

    // Cria um cart novo; o JSON deve vir com userId e username.
    @PostMapping("/cart")
    public ResponseEntity<Cart> createCart(@RequestBody Cart createCart) {
        // Exigimos que createCart.getUserId() esteja preenchido.
        if (createCart.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Cart updatedCart = cartService.createCart(createCart);
        return new ResponseEntity<>(updatedCart, HttpStatus.CREATED);
    }

    @GetMapping("/cart")
    public ResponseEntity<List<Cart>> getAllCart() {
        List<Cart> existcart = cartService.getAllCart();
        return new ResponseEntity<>(existcart, HttpStatus.OK);
    }

    // NOVO endpoint: busca carrinho pelo userId
    @GetMapping("/cart/user/{userId}")
    public ResponseEntity<Cart> getCartByUserId(@PathVariable Long userId) {
        System.out.println("Recebido userId: " + userId);
        Long idLong = null;
        try {
            idLong = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            System.err.println("Parâmetro inválido para userId: " + userId);
            return ResponseEntity.badRequest().build();
        }
        Cart cart = cartService.getCartByUserId(userId);
        if (cart != null) {
            return new ResponseEntity<>(cart, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Antigo (opcional, marcamos como deprecated)
    @Deprecated
    @GetMapping("/cart/{username}")
    public ResponseEntity<Cart> getCartByUsername(@PathVariable String username) {
        Cart cartId = cartService.getCartByUsername(username);
        if (cartId != null) {
            return new ResponseEntity<>(cartId, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}