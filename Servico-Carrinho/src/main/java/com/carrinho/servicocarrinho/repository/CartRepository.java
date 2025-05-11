package com.carrinho.servicocarrinho.repository;

import com.carrinho.servicocarrinho.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUsername(String username);  // Query for Cart by userId
}
