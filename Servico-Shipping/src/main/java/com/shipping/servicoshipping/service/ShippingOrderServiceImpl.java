package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.ShippingOrder;
import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.repository.ShippingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.shipping.servicoshipping.event.KafkaProducerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingOrderServiceImpl implements ShippingOrderService {

    @Autowired
    private ShippingOrderRepository shippingOrderRepository;

    @Autowired
    private OrdersService ordersService;  // <-- Adicionar o OrdersService para podermos criar uma Order!

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Override
    public ShippingOrder createShippingOrder(ShippingOrder shippingOrder) {

        // 1. Primeiro guardar o ShippingOrder na base de dados
        ShippingOrder createdShippingOrder = shippingOrderRepository.save(shippingOrder);

        // 2. Criar automaticamente a Order associada
        Long userId = createdShippingOrder.getUserId();  // <- Pegar o userId (tens de ter o campo na ShippingOrder!)
        Long shippingOrderId = createdShippingOrder.getId();  // <- ID do ShippingOrder acabado de criar

        ordersService.createOrder(userId, shippingOrderId);

        // Construir evento OrderCreated
        String message = String.format("{\"eventType\":\"OrderCreated\",\"userId\":%d,\"orderDate\":%d,\"totalPrice\":%.2f}",
                userId, System.currentTimeMillis(), 0.0);

        kafkaProducerService.sendMessage(message);

        // Construir Evento ShippingCreated
        String shippingMessage = String.format("{\"eventType\":\"ShippingCreated\",\"orderId\":%d,\"firstName\":\"%s\",\"lastName\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"email\":\"%s\",\"postalCode\":\"%s\"}",
                createdShippingOrder.getId(),
                createdShippingOrder.getFirstName(),
                createdShippingOrder.getLastName(),
                createdShippingOrder.getAddress(),
                createdShippingOrder.getCity(),
                createdShippingOrder.getEmail(),
                createdShippingOrder.getPostal_code());

        kafkaProducerService.sendMessage(shippingMessage);

        // 3. Devolver o ShippingOrder criado (como já fazias antes)
        return createdShippingOrder;
    }

    @Override
    public ShippingOrder getById(Long id) {
        return shippingOrderRepository.findById(id).orElse(null);
    }

    @Override
    public List<ShippingOrder> getAllShippingOrders() {
        return shippingOrderRepository.findAll();
    }
}
