package com.shipping.servicoshipping.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    // tópico para o modelo de leitura (CQRS)
    public static final String CQRS_TOPIC = "order-events";
    // tópico para a orquestração Saga
    public static final String SAGA_TOPIC = "checkout-events";

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;

    /** Publica no fluxo de leitura (Query) */
    public void sendToCqrs(String msg) {
        System.out.println("📤 [CQRS] " + msg);
        kafkaTemplate.send(CQRS_TOPIC, msg);
    }

    /** Publica no fluxo de saga */
    public void sendToSaga(String msg) {
        System.out.println("📤 [SAGA] " + msg);
        kafkaTemplate.send(SAGA_TOPIC, msg);
    }
}
