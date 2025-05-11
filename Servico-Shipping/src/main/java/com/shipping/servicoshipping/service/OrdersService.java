package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.Orders;
import org.springframework.stereotype.Service;

@Service
public interface OrdersService {
    Orders createOrder(Long userId, Long shippingOrderId);
    Orders getOrderByShippingOrderId(Long shippingOrderId);
    void updateTotalPrice(Long orderId, double additionalAmount);
    Orders getOrderById(Long orderId);
    boolean finalizeOrder(Long userId);

}
