// Servico-Carrinho/src/main/java/com/carrinho/servicocarrinho/entity/Cart.java
package com.carrinho.servicocarrinho.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartid")
    private Long id;

    // Novo campo
    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    // === Get/Set para userId ===
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // === Get/Set para username ===
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // === Get/Set para createdDate ===
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    // === Get/Set para locked ===
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}