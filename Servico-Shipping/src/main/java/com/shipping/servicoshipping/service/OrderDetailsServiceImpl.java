package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.OrderDetails;
import com.shipping.servicoshipping.repository.OrderDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailsServiceImpl implements OrderDetailsService {
    
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;


    public OrderDetails createOrderDetails(OrderDetails orderDetails){

        return orderDetailsRepository.save(orderDetails);

    }

    @Override
    public OrderDetails getById(Long id) {
        System.out.println("🔍 Procurando OrderDetails com id: " + id);
        List<OrderDetails> todos = orderDetailsRepository.findAll();
        System.out.println("📋 Todos os OrderDetails:");
        todos.forEach(o -> System.out.println("➡️ ID: " + o.getId()));
        return orderDetailsRepository.findById(id).orElse(null);
    }

    @Override
    public List<OrderDetails> getByShippingOrderId(Long shippingOrderId) {
        return orderDetailsRepository.findByShippingOrderId(shippingOrderId);
    }

}
