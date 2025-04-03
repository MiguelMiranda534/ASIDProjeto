package com.ijse.bookstore.service;

import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.OrderDetails;

@Service
public interface OrderDetailsService {
    OrderDetails createOrderDetails(OrderDetails orderDetails);
}
