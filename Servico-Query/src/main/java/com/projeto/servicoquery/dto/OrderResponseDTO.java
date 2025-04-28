package com.projeto.servicoquery.dto;

import java.util.Date;
import java.util.List;

public class OrderResponseDTO {
    private Long orderId;
    private Date orderDate;
    private Double totalPrice;
    private List<OrderItemDTO> items;
    private ShippingDTO shippingDetails;

    // Getters e Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public ShippingDTO getShippingDetails() {
        return shippingDetails;
    }

    public void setShippingDetails(ShippingDTO shippingDetails) {
        this.shippingDetails = shippingDetails;
    }
}
