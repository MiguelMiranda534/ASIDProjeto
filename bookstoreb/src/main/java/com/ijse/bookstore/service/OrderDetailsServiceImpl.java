package com.ijse.bookstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.OrderDetails;
import com.ijse.bookstore.repository.OrderDetailsRepository;

@Service
public class OrderDetailsServiceImpl implements OrderDetailsService{
    
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;


    public OrderDetails createOrderDetails(OrderDetails orderDetails){

        return orderDetailsRepository.save(orderDetails);

    }
}
