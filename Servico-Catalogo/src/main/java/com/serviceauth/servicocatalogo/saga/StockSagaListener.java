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
        this.kafkaTemplate = kafkaTemplate;
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = TOPIC, groupId = "servico-catalogo-saga")
    public void onMessage(String message) {
        System.out.println("📥 [StockSagaListener] recebeu raw: " + message);
        try {
            JsonNode evt     = objectMapper.readTree(message);
            String  typeText = evt.get("eventType").asText();
            System.out.println("➡️ [StockSagaListener] eventoType=" + typeText + ", payload=" + evt.toString());

            // 1) Log de todos os eventos recebidos (para debug)
            System.out.println("📥 StockSagaListener recebeu: " + typeText + " → " + evt.toString());

            // 2) Só nos interessa tratar StockReserveRequested e StockReleaseRequested
            if (!typeText.equals("StockReserveRequested") &&
                    !typeText.equals("StockReleaseRequested")) {
                System.out.println("⏭ Ignorando evento de stock: " + typeText);
                return;
            }

            // 3) A partir daqui sabemos que é um evento válido para stock
            EventType type = EventType.valueOf(typeText);
            String    sagaId = evt.has("sagaId") ? evt.get("sagaId").asText() : null;
            Long      orderId = evt.has("orderId") ? evt.get("orderId").asLong() : null;
            Long      bookId  = evt.has("bookId") ? evt.get("bookId").asLong() : null;
            int       qty     = evt.has("quantity") ? evt.get("quantity").asInt() : 0;

            System.out.println("🔍 [StockSagaListener] processar " + typeText + " → bookId=" + bookId + ", qty=" + qty + ", sagaId=" + sagaId);

            // 4) Log antes de chamar reserve/release
            System.out.println("➡️ Processando evento: " + typeText +
                    " | orderId=" + orderId +
                    " | bookId="  + bookId  +
                    " | qty="     + qty    +
                    " | sagaId="  + sagaId);

            // 5) Dispara o fluxo conforme o tipo
            switch (type) {
                case StockReserveRequested -> reserve(orderId, bookId, qty, sagaId);
                case StockReleaseRequested -> release(orderId, bookId, qty, sagaId);
                default -> { /* nunca chega aqui */ }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void reserve(Long orderId, Long bookId, int qty, String sagaId) {
        System.out.println("🔍 [StockSagaListener.reserve] bookId=" + bookId + ", qty=" + qty
                + ", sagaId=" + sagaId);
        if (bookId == null) {
            System.err.println("⚠️ [StockSagaListener] sem bookId no StockReserveRequested, a falhar reserva");
            publish(StockReserveFailed, orderId, null, qty, sagaId);
            return;
        }
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null && book.getQuantity() >= qty) {
            book.setQuantity(book.getQuantity() - qty);
            bookRepository.save(book);
            System.out.println("✅ Stock reservado: bookId=" + bookId + ", novo stock=" + book.getQuantity());
            publish(StockReserved, orderId, bookId, qty, sagaId);
        } else {
            System.err.println("❌ Falha ao reservar stock: bookId=" + bookId +
                    ", stock disponível=" + (book != null ? book.getQuantity() : "não existe"));
            publish(StockReserveFailed, orderId, bookId, qty, sagaId);
        }
    }

    private void release(Long orderId, Long bookId, int qty, String sagaId) {
        System.out.println("♻️ Libertando stock → bookId=" + bookId + ", quantidade=" + qty);
        if (bookId != null) {
            bookRepository.findById(bookId).ifPresent(b -> {
                b.setQuantity(b.getQuantity() + qty);
                bookRepository.save(b);
                System.out.println("✅ Stock reposto: bookId=" + bookId + ", novo stock=" + b.getQuantity());
            });
        }
        publish(StockReleased, orderId, bookId, qty, sagaId);
    }

    private void publish(EventType type, Long orderId, Long bookId, int qty, String sagaId) {
        ObjectNode payload = objectMapper.createObjectNode()
                .put("eventType", type.name())
                .put("orderId", orderId)
                .put("bookId", bookId)
                .put("quantity", qty);
        if (sagaId != null) {
            payload.put("sagaId", sagaId);
        }
        kafkaTemplate.send(TOPIC, payload.toString());
    }
}