// Servico-SAGA/src/main/java/com/projeto/servicosaga/kafka/KafkaProducerService.java
package com.example.servicosaga.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> template;

    public void send(String topic, String payload) {
        System.out.println("📤 SAGA envia: " + payload);
        template.send(topic, payload);
    }
}