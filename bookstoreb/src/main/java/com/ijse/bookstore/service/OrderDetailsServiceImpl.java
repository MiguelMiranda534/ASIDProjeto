package com.ijse.bookstore.service;

import com.ijse.bookstore.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.OrderDetails;
import com.ijse.bookstore.repository.OrderDetailsRepository;

import java.util.List;

@Service
public class OrderDetailsServiceImpl implements OrderDetailsService{
    
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;


    public OrderDetails createOrderDetails(OrderDetails orderDetails){

        return orderDetailsRepository.save(orderDetails);

    }

    @Override
    public List<Orders> getAllOrders() {
        return null;
    }


}
