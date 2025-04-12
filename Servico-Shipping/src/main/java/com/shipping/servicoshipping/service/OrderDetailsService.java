package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.OrderDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderDetailsService {
    OrderDetails createOrderDetails(OrderDetails orderDetails);
    OrderDetails getById(Long id);
    List<OrderDetails> getByShippingOrderId(Long shippingOrderId);

}
