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
    private KafkaProducerService kafkaProducerService;

    @Override
    public OrderDetails createOrderDetails(OrderDetails orderDetails) {
        System.out.println("➡️ A criar OrderDetails para ShippingOrderID: " + orderDetails.getShippingOrderId());

        // 1. Guardar o novo OrderDetails
        OrderDetails createdDetail = orderDetailsRepository.save(orderDetails);

        // 2. Atualizar o totalPrice da Order
        Orders order = ordersService.getOrderByShippingOrderId(orderDetails.getShippingOrderId());
        if (order != null) {
            System.out.println("📝 Atualizando Order ID: " + order.getId() + " com subTotal: " + createdDetail.getSubTotal());

            ordersService.updateTotalPrice(order.getId(), createdDetail.getSubTotal());

            // Buscar novamente a Order atualizada depois do save
            Orders updatedOrder = ordersService.getOrderById(order.getId());

            // Construir evento
            String message = String.format(
                    "{\"eventType\":\"OrderItemAdded\",\"orderId\":%d,\"bookId\":%d,\"quantity\":%d,\"subTotal\":%.2f}",
                    order.getId(), createdDetail.getBookId(), createdDetail.getQuantity(), createdDetail.getSubTotal()
            );
            kafkaProducerService.sendMessage(message);

            // NOVO: Evento de atualização do Total (com o novo total)
            String totalUpdatedMessage = String.format(
                    "{\"eventType\":\"OrderTotalUpdated\",\"orderId\":%d,\"newTotalPrice\":%.2f}",
                    updatedOrder.getId(), updatedOrder.getTotalPrice()
            );
            kafkaProducerService.sendMessage(totalUpdatedMessage);
        }
        else {
            System.out.println("⚠️ Nenhuma Order encontrada para ShippingOrderID: " + orderDetails.getShippingOrderId());
        }

        return createdDetail;
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
