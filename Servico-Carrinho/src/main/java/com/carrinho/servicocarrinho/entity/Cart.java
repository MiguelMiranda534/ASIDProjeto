package com.carrinho.servicocarrinho.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartid")
    private Long id;

    @Column(name = "user_id") // Direct userId column
    private Long userId; // User's ID

    @Column
    private LocalDate createdDate;
}
