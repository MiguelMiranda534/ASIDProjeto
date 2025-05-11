package com.shipping.servicoshipping.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.servicoshipping.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.shipping.servicoshipping.saga.SagaConstants.TOPIC;
import static com.shipping.servicoshipping.saga.EventType.*;

@Service
public class OrderSagaListener {

    @Autowired private OrdersService orders;
    @Autowired private KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = TOPIC, groupId = "shipping-saga")
    public void listen(String msg) throws Exception {
        Map<String,Object> e = mapper.readValue(msg, Map.class);
        if (!OrderFinalizeRequested.name().equals(e.get("eventType"))) return;

        Long userId = Long.valueOf(e.get("userId").toString());
        boolean ok = orders.finalizeOrder(userId);

        EventType reply = ok ? OrderFinalized : OrderFinalizeFailed;
        var payload = Map.of("eventType", reply.name(), "userId", userId);
        // aqui também usa apenas o teu TOPIC
        kafka.send(TOPIC, mapper.writeValueAsString(payload));
    }
}