// Servico-Carrinho/src/main/java/com/carrinho/servicocarrinho/saga/CartSagaListener.java
package com.carrinho.servicocarrinho.saga;

import com.carrinho.servicocarrinho.service.CartItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.carrinho.servicocarrinho.saga.SagaConstants.TOPIC;
import static com.carrinho.servicocarrinho.saga.EventType.*;

@Service
public class CartSagaListener {

    @Autowired private CartItemService cart;
    @Autowired private KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = SagaConstants.TOPIC, groupId = "carrinho-saga")
    public void listen(String msg) throws Exception {
        Map<String,Object> e = mapper.readValue(msg, Map.class);
        if (!EventType.CartClearRequested.name().equals(e.get("eventType"))) return;

        String userId = e.get("userId").toString();
        cart.clearCartForUser(userId);   // <<< apaga só deste user
        send(EventType.CartCleared, Map.of("userId", userId));
    }

    private void send(EventType type, Map<String,Object> body) throws Exception {
        var m = new java.util.HashMap<>(body);
        m.put("eventType", type.name());
        kafka.send(SagaConstants.TOPIC, mapper.writeValueAsString(m));
    }
}