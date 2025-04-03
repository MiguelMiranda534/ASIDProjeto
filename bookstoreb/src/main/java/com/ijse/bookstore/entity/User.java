package com.ijse.bookstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    private Long id;

    @Column
    private String fullname;

    @Column(unique = true)
    private String username;

    @Column 
    private String password;

    @Column(unique = true)
    private String email;

    @OneToOne
    @JoinColumn(name = "shipping_information_id")
    private ShippingOrder shippingOrder;
}
