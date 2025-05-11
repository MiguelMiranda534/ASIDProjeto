package com.serviceauth.servicocatalogo.saga;

import static com.serviceauth.servicocatalogo.saga.SagaConstants.TOPIC;
import static com.serviceauth.servicocatalogo.saga.EventType.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceauth.servicocatalogo.entity.Book;
import com.serviceauth.servicocatalogo.repository.BookRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockSagaListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    public StockSagaListener(KafkaTemplate<String, String> kafkaTemplate,
                             BookRepository bookRepository,
                             ObjectMapper objectMapper) {
        this.kafkaTemplate   = kafkaTemplate;
        this.bookRepository  = bookRepository;
        this.objectMapper    = objectMapper;
    }

    @KafkaListener(topics = TOPIC, groupId = "servico-catalogo-saga")
    public void onMessage(String message) {
        try {
            JsonNode evt = objectMapper.readTree(message);
            EventType type = EventType.valueOf(evt.get("eventType").asText());
            Long orderId   = evt.has("orderId")   ? evt.get("orderId").asLong()   : null;
            Long bookId    = evt.has("bookId")    ? evt.get("bookId").asLong()    : null;
            int  qty       = evt.has("quantity")  ? evt.get("quantity").asInt()   : 0;

            switch (type) {
                case StockReserveRequested:
                    reserve(orderId, bookId, qty);
                    break;
                case StockReleaseRequested:
                    release(orderId, bookId, qty);
                    break;
                default:
                    // outros eventos são ignorados pelo catálogo
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void reserve(Long orderId, Long bookId, int qty) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null && book.getQuantity() >= qty) {
            book.setQuantity(book.getQuantity() - qty);
            bookRepository.save(book);
            publish(StockReserved, orderId, bookId, qty);
        } else {
            publish(StockReserveFailed, orderId, bookId, qty);
        }
    }

    private void release(Long orderId, Long bookId, int qty) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            book.setQuantity(book.getQuantity() + qty);
            bookRepository.save(book);
        }
        publish(StockReleased, orderId, bookId, qty);
    }

    private void publish(EventType type, Long orderId, Long bookId, int qty) {
        ObjectNode payload = objectMapper
                .createObjectNode()
                .put("eventType", type.name())
                .put("orderId", orderId)
                .put("bookId", bookId)
                .put("quantity", qty);
        kafkaTemplate.send(TOPIC, payload.toString());
    }
}