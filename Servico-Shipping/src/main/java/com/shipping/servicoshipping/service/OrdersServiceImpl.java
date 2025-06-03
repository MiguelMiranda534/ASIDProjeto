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
        Orders o = new Orders();
        o.setUserId(userId);
        o.setOrderDate(new Date());
        o.setTotalPrice(0.0);
        o.setShippingOrderID(shippingOrderId);
        o.setStatus("PENDING");
        return ordersRepository.save(o);
    }

    @Override
    public Orders getOrderByShippingOrderId(Long shippingOrderId) {
        return ordersRepository.findByShippingOrderID(shippingOrderId);
    }

    @Override
    public void updateTotalPrice(Long orderId, double additionalAmount) {
        Orders o = ordersRepository.findById(orderId).orElse(null);
        if (o != null) {
            o.setTotalPrice(o.getTotalPrice() + additionalAmount);
            ordersRepository.save(o);
        }
    }

    @Override                     // ---------- FIX ----------
    public boolean finalizeOrder(Long orderId) {
        Orders o = ordersRepository.findById(orderId).orElse(null);
        if (o == null || !"PENDING".equals(o.getStatus())) return false;
        o.setStatus("CLOSED");
        ordersRepository.save(o);
        return true;
    }

    @Override
    public Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId).orElse(null);
    }
}