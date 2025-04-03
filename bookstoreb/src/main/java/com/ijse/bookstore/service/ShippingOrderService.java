package com.ijse.bookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.ShippingOrder;



@Service
public interface ShippingOrderService {
    
    ShippingOrder createShippingOrder(ShippingOrder shippingOrder);

    List<ShippingOrder> getAllShippingOrders();
}
