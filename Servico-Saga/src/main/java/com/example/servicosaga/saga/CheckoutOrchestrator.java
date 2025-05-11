package com.example.servicosaga.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.servicosaga.kafka.KafkaProducerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.servicosaga.saga.SagaConstants.TOPIC;

@Service
public class CheckoutOrchestrator {

    @Autowired
    private KafkaProducerService producer;

    private final ObjectMapper mapper = new ObjectMapper();

    /* === Chamada pelo controller === */
    public void startSaga(Long userId) {
        send(EventType.StockReserveRequested, Map.of("userId", userId));
    }

    /* === Ouvir todos os eventos da SAGA === */
    @KafkaListener(topics = TOPIC, groupId = "saga-orchestrator-group")
    public void listen(ConsumerRecord<String, String> rec) throws Exception {
        Map<String, Object> event = mapper.readValue(rec.value(), Map.class);
        EventType type = EventType.valueOf(event.get("eventType").toString());
        Long userId = Long.valueOf(event.get("userId").toString());

        switch (type) {
            case StockReserved ->
                    send(EventType.CartClearRequested, Map.of("userId", userId));
            case CartCleared ->
                    send(EventType.OrderFinalizeRequested, Map.of("userId", userId));
            case OrderFinalized ->
                    send(EventType.CheckoutCompleted, Map.of("userId", userId));
            /* --- falhas e compensações --- */
            case StockReserveFailed ->
                    send(EventType.CheckoutFailed, Map.of("userId", userId, "reason", "stock unavailable"));
            case OrderFinalizeFailed -> {
                send(EventType.StockReleaseRequested, Map.of("userId", userId));
                /* Quando StockReleased chegar, termina como falhado */
            }
            case StockReleased ->
                    send(EventType.CheckoutFailed, Map.of("userId", userId, "reason", "order rollback"));
            default -> { /* ignora */ }
        }
    }

    /* === utilitário === */
    private void send(EventType type, Map<String, Object> body) {
        try {
            var copy = new java.util.HashMap<>(body);
            copy.put("eventType", type.name());
            producer.send(TOPIC, mapper.writeValueAsString(copy));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}