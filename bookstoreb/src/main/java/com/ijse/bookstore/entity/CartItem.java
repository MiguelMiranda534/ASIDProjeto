package com.ijse.bookstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book bookid;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user;
    
}
