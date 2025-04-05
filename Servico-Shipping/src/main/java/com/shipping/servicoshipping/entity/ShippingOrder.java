package com.shipping.servicoshipping.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class ShippingOrder {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shippingorder_id")
    private Long id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String address;

    @Column 
    private String city;

    @Column 
    private String email;

    @Column 
    private String postal_code;

}
