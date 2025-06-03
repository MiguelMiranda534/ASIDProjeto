package com.shipping.servicoshipping.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderid")
    private Long id;

    @Column
    private Date orderDate;

    @Column
    private double totalPrice;

    @Column(name = "shippingorder_id")
    private Long shippingOrderID;

    @Column
    private Long userId;

    @Column
    private String status = "PENDING"; // Novo campo para status (PENDING/CLOSED)

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getShippingOrderID() {
        return shippingOrderID;
    }

    public void setShippingOrderID(Long shippingOrderID) {
        this.shippingOrderID = shippingOrderID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}