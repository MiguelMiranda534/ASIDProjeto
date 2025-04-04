package com.carrinho.servicocarrinho.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    private int quantity;

    @Column
    private Double unitPrice;

    @Column
    private Double subTotal;

    // Store the username (or userId) of the user who owns the cart item
    @Column(name = "username") // You could also store a userId if necessary
    private String userId;
}
