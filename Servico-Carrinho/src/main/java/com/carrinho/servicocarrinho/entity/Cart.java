package com.carrinho.servicocarrinho.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartid")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column
    private LocalDate createdDate;

    @Column(name = "locked")
    private boolean locked = false;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isLocked()            { return locked; }

    public void setLocked(boolean l)     { this.locked = l; }
}