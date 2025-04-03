package com.carrinho.servicocarrinho.controller;

import com.carrinho.servicocarrinho.entity.Cart;
import com.carrinho.servicocarrinho.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
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
