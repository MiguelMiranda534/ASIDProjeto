package com.carrinho.servicocarrinho.repository;

import com.carrinho.servicocarrinho.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    @Transactional
    @Modifying
    @Query(value = "ALTER TABLE cart_item AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

    // Métodos antigos para username (você pode manter ou remover mais tarde)
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.username = :username")
    void deleteByUsername(@Param("username") String username);

    List<CartItem> findByUsername(String username);

    // === NOVOS métodos para user_id ===
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    List<CartItem> findByUserId(Long userId);
}