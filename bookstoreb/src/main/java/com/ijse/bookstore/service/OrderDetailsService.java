package com.ijse.bookstore.service;

import com.ijse.bookstore.entity.Cart;
import com.ijse.bookstore.entity.Orders;
import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.OrderDetails;

import java.util.List;

@Service
public interface OrderDetailsService {
    OrderDetails createOrderDetails(OrderDetails orderDetails);

    List<Orders> getAllOrders();
}
