package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.ShippingOrder;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public interface ShippingOrderService {
    
    ShippingOrder createShippingOrder(ShippingOrder shippingOrder);

    List<ShippingOrder> getAllShippingOrders();
}
