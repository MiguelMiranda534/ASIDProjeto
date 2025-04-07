package com.shipping.servicoshipping.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data

public class Orders {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderid")
    private Long id;

    @Column
    private Date orderDate;

    @Column
    private double totalPrice;

}
