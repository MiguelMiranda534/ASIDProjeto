package com.carrinho.servicocarrinho.controller;

import com.carrinho.servicocarrinho.entity.CartItem;
import com.carrinho.servicocarrinho.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/cart/cartitem")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    // Cria um CartItem; o JSON deve incluir userId e opcional username
    @PostMapping("/add")
    public ResponseEntity<CartItem> createCartItem(@RequestBody CartItem cartItem) {
        if (cartItem.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        cartItem.setSubTotal(cartItem.getUnitPrice() * cartItem.getQuantity());
        CartItem createdCartItem = cartItemService.createCartItem(cartItem);
        return new ResponseEntity<>(createdCartItem, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<CartItem>> getAllCartItem() {
        List<CartItem> cartItems = cartItemService.getAllCartitem();
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItem> getCartItemById(@PathVariable Long id) {
        CartItem existCartItem = cartItemService.getCartItemById(id);
        if (existCartItem != null) {
            return new ResponseEntity<>(existCartItem, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // === NOVO: buscar CartItem pelo userId ===
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartItem>> getCartItemsByUserId(@PathVariable Long userId) {
        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(userId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    // Antigo
    @Deprecated
    @GetMapping("/username/{username}")
    public ResponseEntity<List<CartItem>> getCartItemByUsername(@PathVariable String username) {
        List<CartItem> cartItems = cartItemService.getCartItemsByUsername(username);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @PatchMapping("/quantity/{id}")
    public ResponseEntity<CartItem> patchCartQuantity(@PathVariable Long id, @RequestBody CartItem cartItem) {
        CartItem patchedCartItem = cartItemService.patchCartQuantity(id, cartItem);
        return new ResponseEntity<>(patchedCartItem, HttpStatus.CREATED);
    }

    @PatchMapping("/subtotal/{id}")
    public ResponseEntity<CartItem> patchCartSubTotal(@PathVariable Long id, @RequestBody CartItem cartItem) {
        CartItem patchedCartItem = cartItemService.patchCartSubTotal(id, cartItem);
        return new ResponseEntity<>(patchedCartItem, HttpStatus.CREATED);
    }

    @DeleteMapping("/clearcart")
    public ResponseEntity<String> clearCart() {
        cartItemService.clearCart();
        return ResponseEntity.ok("Cart cleared and Id reset.");
    }

    @PostMapping("/reset")
    public void resetAutoIncrement() {
        cartItemService.resetAutoIncrement();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CartItem> deleteCartItyItemById(@PathVariable Long id) {
        cartItemService.deleteCartItyItemById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}