package com.ijse.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ijse.bookstore.entity.CartItem;
import com.ijse.bookstore.service.CartItemService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;



    @PostMapping("/cartitem")
    public ResponseEntity<CartItem> createCartItem(@RequestBody CartItem cartItem){

        CartItem createdCartItem = cartItemService.createCartItem(cartItem);

        return new ResponseEntity<>(createdCartItem,HttpStatus.CREATED);
 
         
    }

    @GetMapping("/cartitem")
    public ResponseEntity<List<CartItem>> getAllCartItem(){

        List<CartItem> cartItems = cartItemService.getAllCartitem();

        return new ResponseEntity<>(cartItems,HttpStatus.OK);
    }

    @GetMapping("/cartitem/{id}")
    public ResponseEntity<CartItem> getCartItemById(@PathVariable Long id){

        CartItem existCartItem  = cartItemService.getCartItemById(id);

        if(existCartItem != null) {
            return new ResponseEntity<>(existCartItem , HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cartitem/user/{username}")
    public ResponseEntity<List<CartItem>> getCartItemByUsername(@PathVariable String username) {
        List<CartItem> cartItems = cartItemService.getCartItemsByUsername(username);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }
    

    @PatchMapping("/quantity/{id}")
    public ResponseEntity<CartItem> patchCartQuantity(@PathVariable Long id , @RequestBody CartItem cartItem){

        CartItem patchedCartItem = cartItemService.patchCartQuantity(id,cartItem);

        return new ResponseEntity<>(patchedCartItem,HttpStatus.CREATED);
    }

    @PatchMapping("/subtotal/{id}")
    public ResponseEntity<CartItem> patchCartSubTotal(@PathVariable Long id , @RequestBody CartItem cartItem){

        CartItem patchedCartItem = cartItemService.patchCartSubTotal(id,cartItem);

        return new ResponseEntity<>(patchedCartItem,HttpStatus.CREATED);
    }

    @DeleteMapping("/clearcart")
    public ResponseEntity<String> clearCart(){

        cartItemService.clearCart();
        return ResponseEntity.ok("Cart cleared and Id reset.");
    }

    @PostMapping("/reset")
    public void resetAutoIncrement() {
        cartItemService.resetAutoIncrement();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CartItem> deleteCartItyItemById(@PathVariable Long id){

        cartItemService.deleteCartItyItemById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    } 
}
