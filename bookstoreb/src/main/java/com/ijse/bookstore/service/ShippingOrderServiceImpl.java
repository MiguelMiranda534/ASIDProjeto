package com.ijse.bookstore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ijse.bookstore.entity.ShippingOrder;
import com.ijse.bookstore.repository.ShippingOrderRepository;

@Service
public class ShippingOrderServiceImpl implements ShippingOrderService{
    
    @Autowired
    private ShippingOrderRepository shippingOrderRepository;


    @Override
    public ShippingOrder createShippingOrder(ShippingOrder shippingOrder){

        return shippingOrderRepository.save(shippingOrder);

    }

    public List<ShippingOrder> getAllShippingOrders(){

        
        return shippingOrderRepository.findAll();
    }
}
