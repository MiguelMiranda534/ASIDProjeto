package com.carrinho.servicocarrinho.repository;

import com.carrinho.servicocarrinho.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Manter para compatibilidade, mas passaremos a usar o novo:
    Cart findByUsername(String username);

    // Novo método para buscar pelo user_id:
    Cart findByUserId(Long userId);
}