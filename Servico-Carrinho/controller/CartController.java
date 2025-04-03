package com.ijse.bookstore.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ijse.bookstore.entity.Cart;
import com.ijse.bookstore.repository.UserRepository;
import com.ijse.bookstore.service.CartService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {
    
    @Autowired
    private CartService cartService;
    // private UserRepository userRepository;

    @PostMapping("/cart")
    public ResponseEntity<Cart> createCart(@RequestBody Cart createCart){

        Cart updatedCart = cartService.createCart(createCart);

      
        
        return new ResponseEntity<>(updatedCart,HttpStatus.CREATED);
    }

    @GetMapping("/cart")
    public ResponseEntity<List<Cart>> getAllCart(){

        List<Cart> existcart = cartService.getAllCart();

        return new ResponseEntity<>(existcart,HttpStatus.OK);
    }

    @GetMapping("/cart/{userId}")
    public ResponseEntity<Cart> getCartIdByUserId(@PathVariable Long userId){

        Cart cartId = cartService.getCartIdByUserId(userId);

        if (cartId != null) {
            return new ResponseEntity<>(cartId,HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }


    }

}
