package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.OrderDetails;
import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.event.KafkaProducerService;
import com.shipping.servicoshipping.repository.OrderDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailsServiceImpl implements OrderDetailsService {

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrdersService ordersService; // <<< IMPORTANTE

    @Autowired
    private KafkaProducerService   kafkaProducerService;


    @Override
    public OrderDetails createOrderDetails(OrderDetails orderDetails) {
        System.out.println("➡️  A criar OrderDetails para ShippingOrderID: "
                + orderDetails.getShippingOrderId());

        /* 1) Guardar o detalhe */
        OrderDetails created = orderDetailsRepository.save(orderDetails);

        /* 2) Buscar/actualizar a encomenda */
        Orders order = ordersService.getOrderByShippingOrderId(
                orderDetails.getShippingOrderId());
        if (order != null) {

            /* 2a) pedir reserva de stock */
            kafkaProducerService.sendToSaga(String.format(
                    "{\"eventType\":\"StockReserveRequested\"," +
                            "\"orderId\":%d,\"bookId\":%d,\"quantity\":%d}",
                    order.getId(),
                    created.getBookId(),
                    created.getQuantity()
            ));

            /* 2b) actualizar total */
            ordersService.updateTotalPrice(order.getId(), created.getSubTotal());
            Orders updated = ordersService.getOrderById(order.getId());

            /* 2c) eventos já existentes (CQRS + Saga) */
            String itemAdded = String.format(
                    "{\"eventType\":\"OrderItemAdded\",\"orderId\":%d," +
                            "\"bookId\":%d,\"quantity\":%d,\"subTotal\":%.2f}",
                    order.getId(),
                    created.getBookId(),
                    created.getQuantity(),
                    created.getSubTotal()
            );
            kafkaProducerService.sendToSaga(itemAdded);
            kafkaProducerService.sendToCqrs(itemAdded);

            String totalUpdated = String.format(
                    "{\"eventType\":\"OrderTotalUpdated\",\"orderId\":%d," +
                            "\"newTotalPrice\":%.2f}",
                    updated.getId(),
                    updated.getTotalPrice()
            );
            kafkaProducerService.sendToSaga(totalUpdated);
            kafkaProducerService.sendToCqrs(totalUpdated);

        } else {
            System.out.println("⚠️  Nenhuma Order encontrada para ShippingOrderID: "
                    + orderDetails.getShippingOrderId());
        }
        return created;
    }

    @Override
    public OrderDetails getById(Long id) {
        System.out.println("🔍 Procurando OrderDetails com id: " + id);
        List<OrderDetails> todos = orderDetailsRepository.findAll();
        System.out.println("📋 Todos os OrderDetails:");
        todos.forEach(o -> System.out.println("➡️ ID: " + o.getId()));
        return orderDetailsRepository.findById(id).orElse(null);
    }

    @Override
    public List<OrderDetails> getByShippingOrderId(Long shippingOrderId) {
        return orderDetailsRepository.findByShippingOrderId(shippingOrderId);
    }
}
