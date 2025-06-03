package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.ShippingOrder;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
// interface
public interface ShippingOrderService {
    ShippingOrder createShippingOrder(ShippingOrder shippingOrder, String sagaId);
    ShippingOrder getById(Long id);
    List<ShippingOrder> getAllShippingOrders();
}
