package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.OrderDetails;
import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.event.KafkaProducerService;
import com.shipping.servicoshipping.repository.OrderDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class OrderDetailsServiceImpl implements OrderDetailsService {

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    // Um WebClient simples para buscar dados ao Serviço‐Catálogo
    private final WebClient catalogWebClient = WebClient.create("http://servico-catalogo:8082");

    @Override
    public OrderDetails createOrderDetails(OrderDetails orderDetails) {
        System.out.println("➡️  A criar OrderDetails para ShippingOrderID: "
                + orderDetails.getShippingOrderId());

        OrderDetails created = orderDetailsRepository.save(orderDetails);

        Orders order = ordersService.getOrderByShippingOrderId(
                orderDetails.getShippingOrderId());
        if (order != null) {
            // Atualiza o totalPrice no Orders
            ordersService.updateTotalPrice(order.getId(), created.getSubTotal());
            Orders updated = ordersService.getOrderById(order.getId());

            // BUSCA os detalhes do livro (title, authorName, price) de forma bloqueante:
            Map<String, Object> bookMap = null;
            try {
                bookMap = catalogWebClient.get()
                        .uri("/catalogo/books/{id}", created.getBookId())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
            } catch (Exception e) {
                System.err.println("❌ Não foi possível buscar detalhes do livro do Catálogo: " + e.getMessage());
            }

            // Extrai title, authorName e price (se existirem)
            String bookTitle = "";
            String authorName = "";
            Double bookPrice = 0.0;

            if (bookMap != null) {
                Object titleObj = bookMap.get("title");
                if (titleObj != null) {
                    bookTitle = titleObj.toString();
                }
                Object authorObj = bookMap.get("author");
                if (authorObj instanceof Map<?, ?>) {
                    Object authorNameObj = ((Map<?, ?>) authorObj).get("authorName");
                    if (authorNameObj != null) {
                        authorName = authorNameObj.toString();
                    }
                }
                Object priceObj = bookMap.get("price");
                if (priceObj instanceof Number) {
                    bookPrice = ((Number) priceObj).doubleValue();
                } else if (priceObj instanceof String) {
                    try {
                        bookPrice = Double.parseDouble((String) priceObj);
                    } catch (Exception ignored) {}
                }
            }

            // Monta o JSON enriquecido de OrderItemAdded (inclui título, autor e preço)
            String itemAdded = String.format(
                    "{" +
                            "\"eventType\":\"OrderItemAdded\"," +
                            "\"orderId\":%d," +
                            "\"bookId\":%d," +
                            "\"quantity\":%d," +
                            "\"subTotal\":%.2f," +
                            "\"bookTitle\":\"%s\"," +
                            "\"authorName\":\"%s\"," +
                            "\"bookPrice\":%.2f" +
                            "}",
                    order.getId(),
                    created.getBookId(),
                    created.getQuantity(),
                    created.getSubTotal(),
                    escapeJson(bookTitle),
                    escapeJson(authorName),
                    bookPrice
            );
            kafkaProducerService.sendToSaga(itemAdded);
            kafkaProducerService.sendToCqrs(itemAdded);

            // Continua publicando o evento de total atualizado
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
        return orderDetailsRepository.findById(id).orElse(null);
    }

    @Override
    public java.util.List<OrderDetails> getByShippingOrderId(Long shippingOrderId) {
        return orderDetailsRepository.findByShippingOrderId(shippingOrderId);
    }

    // Método utilitário para escapar barras e aspas em strings JSON
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}