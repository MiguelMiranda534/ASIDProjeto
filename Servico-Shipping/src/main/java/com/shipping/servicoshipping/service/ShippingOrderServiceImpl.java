package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.ShippingOrder;
import com.shipping.servicoshipping.repository.ShippingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingOrderServiceImpl implements ShippingOrderService {
    
    @Autowired
    private ShippingOrderRepository shippingOrderRepository;


    @Override
    public ShippingOrder createShippingOrder(ShippingOrder shippingOrder){

        return shippingOrderRepository.save(shippingOrder);

    }
    @Override
    public ShippingOrder getById(Long id) {
        return shippingOrderRepository.findById(id).orElse(null);
    }

    public List<ShippingOrder> getAllShippingOrders(){

        
        return shippingOrderRepository.findAll();
    }

}
