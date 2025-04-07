package com.shipping.servicoshipping.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detailsid")
    private Long id;

    @Column
    private int quantity;

    @Column
    private double subTotal;

}
