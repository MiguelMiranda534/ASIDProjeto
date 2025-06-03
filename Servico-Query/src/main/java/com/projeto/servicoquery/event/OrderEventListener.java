package com.projeto.servicoquery.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.servicoquery.entity.QueryOrder;
import com.projeto.servicoquery.entity.QueryOrderItem;
import com.projeto.servicoquery.entity.QueryShipping;
import com.projeto.servicoquery.repository.QueryOrderItemRepository;
import com.projeto.servicoquery.repository.QueryOrderRepository;
import com.projeto.servicoquery.repository.QueryShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class OrderEventListener {

    @Autowired
    private QueryOrderRepository queryOrderRepository;

    @Autowired
    private QueryOrderItemRepository queryOrderItemRepository;

    @Autowired
    private QueryShippingRepository queryShippingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "order-events", groupId = "servico-query-group")
    public void listenOrderEvents(String message) {
        try {
            System.out.println("📥 Recebido evento: " + message);

            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");

            switch (eventType) {
                case "OrderCreated" -> {
                    Long orderId = Long.valueOf(event.get("orderId").toString());

                    QueryOrder order = new QueryOrder();
                    order.setId(orderId);                       // garante que coincide com o orderId real
                    order.setUserId(Long.valueOf(event.get("userId").toString()));
                    order.setOrderDate(new Date(Long.parseLong(event.get("orderDate").toString())));
                    order.setTotalPrice(Double.parseDouble(event.get("totalPrice").toString()));
                    order.setStatus("PENDING");
                    queryOrderRepository.save(order);
                }
                case "OrderItemAdded" -> {
                    QueryOrderItem item = new QueryOrderItem();
                    item.setOrderId(Long.valueOf(event.get("orderId").toString()));
                    item.setBookId(Long.valueOf(event.get("bookId").toString()));
                    item.setQuantity(Integer.parseInt(event.get("quantity").toString()));
                    item.setSubTotal(Double.parseDouble(event.get("subTotal").toString()));
                    queryOrderItemRepository.save(item);
                }
                case "ShippingCreated" -> {
                    QueryShipping shipping = new QueryShipping();
                    shipping.setOrderId(Long.valueOf(event.get("orderId").toString()));
                    shipping.setFirstName((String) event.get("firstName"));
                    shipping.setLastName((String) event.get("lastName"));
                    shipping.setAddress((String) event.get("address"));
                    shipping.setCity((String) event.get("city"));
                    shipping.setEmail((String) event.get("email"));
                    shipping.setPostalCode((String) event.get("postalCode"));
                    queryShippingRepository.save(shipping);
                }
                case "OrderTotalUpdated" -> {
                    Long orderId = Long.valueOf(event.get("orderId").toString());
                    Double newTotalPrice = Double.parseDouble(event.get("newTotalPrice").toString());

                    queryOrderRepository.findById(orderId).ifPresent(o -> {
                        o.setTotalPrice(newTotalPrice);
                        queryOrderRepository.save(o);
                    });
                }
                case "OrderFinalized" -> {
                    Long orderId = Long.valueOf(event.get("orderId").toString());
                    queryOrderRepository.findById(orderId).ifPresent(o -> {
                        o.setStatus("CLOSED");
                        queryOrderRepository.save(o);
                    });
                }
                default -> System.out.println("⚠️ Tipo de evento desconhecido: " + eventType);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro ao processar evento: " + e.getMessage());
            e.printStackTrace();
        }
    }
}