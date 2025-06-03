package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.ShippingOrder;
import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.repository.ShippingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.shipping.servicoshipping.event.KafkaProducerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShippingOrderServiceImpl implements ShippingOrderService {

    @Autowired
    private ShippingOrderRepository shippingOrderRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Override
    public ShippingOrder createShippingOrder(ShippingOrder shippingOrder) {
        ShippingOrder created = shippingOrderRepository.save(shippingOrder);
        Long userId = created.getUserId();
        Long shippingOrderId = created.getId();
        Orders order = ordersService.createOrder(userId, shippingOrderId);

        String sagaId = UUID.randomUUID().toString(); // Gerar sagaId aqui ou receber do Saga
        String orderCreated = String.format(
                "{\"eventType\":\"OrderCreated\",\"userId\":%d,\"orderDate\":%d,\"totalPrice\":%.2f,\"orderId\":%d,\"sagaId\":\"%s\"}",
                userId, System.currentTimeMillis(), 0.0, order.getId(), sagaId
        );
        kafkaProducerService.sendToSaga(orderCreated);
        kafkaProducerService.sendToCqrs(orderCreated);

        String shippingCreated = String.format(
                "{\"eventType\":\"ShippingCreated\",\"orderId\":%d," +
                        "\"firstName\":\"%s\",\"lastName\":\"%s\",\"address\":\"%s\"," +
                        "\"city\":\"%s\",\"email\":\"%s\",\"postalCode\":\"%s\",\"sagaId\":\"%s\"}",
                shippingOrderId,
                created.getFirstName(),
                created.getLastName(),
                created.getAddress(),
                created.getCity(),
                created.getEmail(),
                created.getPostal_code(),
                sagaId
        );
        kafkaProducerService.sendToSaga(shippingCreated);
        kafkaProducerService.sendToCqrs(shippingCreated);

        return created;
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