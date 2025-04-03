package com.ijse.bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ijse.bookstore.entity.Cart;


@Repository
public interface CartRepository extends JpaRepository <Cart , Long>{

    Cart getCartIdByUserId(Long userId);

}