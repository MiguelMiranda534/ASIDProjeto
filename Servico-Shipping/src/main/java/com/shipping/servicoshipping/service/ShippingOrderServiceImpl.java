package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.ShippingOrder;
import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.repository.ShippingOrderRepository;
import jakarta.transaction.Transactional;
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
    @Transactional
    public ShippingOrder createShippingOrder(ShippingOrder shippingOrder, String sagaId) {

        System.out.println("🚀 [ShippingOrderService] createShippingOrder chamado com sagaId=" + sagaId
                + ", userId=" + shippingOrder.getUserId());

        ShippingOrder created = shippingOrderRepository.save(shippingOrder);
        System.out.println("✅ [ShippingOrderService] ShippingOrder salvo: " + created);

        Long userId        = created.getUserId();
        Long shippingOrderId = created.getId();

        // 1) criar a Orders (no mesmo serviço) usando o mesmo userId
        Orders order = ordersService.createOrder(userId, shippingOrderId);
        System.out.println("✅ [ShippingOrderService] Orders criada: " + order);

        // 2) Agora publicamos os eventos usando O MESMO sagaId recebido
        String orderCreated = String.format(
                "{\"eventType\":\"OrderCreated\",\"userId\":%d,\"orderDate\":%d,\"totalPrice\":%.2f,\"orderId\":%d,\"sagaId\":\"%s\"}",
                userId, System.currentTimeMillis(), 0.0, order.getId(), sagaId
        );
        System.out.println("📤 [ShippingOrderService] a publicar evento OrderCreated: " + orderCreated);
        kafkaProducerService.sendToSaga(orderCreated);
        kafkaProducerService.sendToCqrs(orderCreated);

        String shippingCreated = String.format(
                "{\"eventType\":\"ShippingCreated\","
                        + "\"orderId\":%d,"            // ← O orderId correto
                        + "\"shippingOrderId\":%d,"    // ← (opcional) se quiser persistir esse valor noutra coluna, mas não é usado pelo Query
                        + "\"firstName\":\"%s\",\"lastName\":\"%s\",\"address\":\"%s\","
                        + "\"city\":\"%s\",\"email\":\"%s\",\"postalCode\":\"%s\",\"sagaId\":\"%s\"}",
                // aqui usamos order.getId() e depois shippingOrderId:
                order.getId(),
                shippingOrderId,
                created.getFirstName(),
                created.getLastName(),
                created.getAddress(),
                created.getCity(),
                created.getEmail(),
                created.getPostal_code(),
                sagaId
        );
        System.out.println("📤 [ShippingOrderService] a publicar evento ShippingCreated: " + shippingCreated);
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