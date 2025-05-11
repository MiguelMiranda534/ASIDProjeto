package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Override
    public Orders createOrder(Long userId, Long shippingOrderId) {
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderDate(new Date());
        order.setTotalPrice(0.0);
        order.setShippingOrderID(shippingOrderId);
        return ordersRepository.save(order);
    }

    @Override
    public Orders getOrderByShippingOrderId(Long shippingOrderId) {
        return ordersRepository.findByShippingOrderID(shippingOrderId);
    }

    @Override
    public void updateTotalPrice(Long orderId, double additionalAmount) {
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setTotalPrice(order.getTotalPrice() + additionalAmount);
            ordersRepository.save(order);
        }
    }

    @Override
    public boolean finalizeOrder(Long userId) {
        // Exemplo mínimo: apenas marca como "finalizado" se existir
        Orders order = ordersRepository.findByUserId(userId);
        if (order == null) return false;
        // Aqui, se quisesses, podias fazer algo como:
        // order.setStatus("FINALIZED");
        // ordersRepository.save(order);
        return true;
    }

    @Override
    public Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId).orElse(null);
    }

}
