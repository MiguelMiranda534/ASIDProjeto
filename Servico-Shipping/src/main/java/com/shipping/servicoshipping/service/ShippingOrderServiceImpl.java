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
        // 1) Guardar o ShippingOrder
        ShippingOrder created = shippingOrderRepository.save(shippingOrder);

        // 2) Criar automaticamente a Order associada
        Long userId          = created.getUserId();
        Long shippingOrderId = created.getId();
        ordersService.createOrder(userId, shippingOrderId);

        // 3) Disparar evento OrderCreated
        String orderCreated = String.format(
                "{\"eventType\":\"OrderCreated\",\"userId\":%d,\"orderDate\":%d,\"totalPrice\":%.2f}",
                userId, System.currentTimeMillis(), 0.0
        );
        kafkaProducerService.sendToSaga(orderCreated);
        kafkaProducerService.sendToCqrs(orderCreated);

        // 4) Disparar evento ShippingCreated
        String shippingCreated = String.format(
                "{\"eventType\":\"ShippingCreated\",\"orderId\":%d," +
                        "\"firstName\":\"%s\",\"lastName\":\"%s\",\"address\":\"%s\"," +
                        "\"city\":\"%s\",\"email\":\"%s\",\"postalCode\":\"%s\"}",
                shippingOrderId,
                created.getFirstName(),
                created.getLastName(),
                created.getAddress(),
                created.getCity(),
                created.getEmail(),
                created.getPostal_code()
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
