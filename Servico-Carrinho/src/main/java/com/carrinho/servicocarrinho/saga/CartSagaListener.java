package com.carrinho.servicocarrinho.saga;

import com.carrinho.servicocarrinho.entity.CartItem;
import com.carrinho.servicocarrinho.service.CartItemService;
import com.carrinho.servicocarrinho.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.carrinho.servicocarrinho.saga.SagaConstants.TOPIC;
import static com.carrinho.servicocarrinho.saga.EventType.*;

@Service
public class CartSagaListener {

    @Autowired private CartItemService cartItemService;
    @Autowired private CartService cartService;
    @Autowired private KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = SagaConstants.TOPIC, groupId = "carrinho-saga")
    public void listen(String msg) throws Exception {
        Map<String,Object> e = mapper.readValue(msg, Map.class);

        // Handler para pedido de lock do carrinho
        if (CartLockRequested.name().equals(e.get("eventType"))) {
            String userId = e.get("userId").toString();
            String sagaId = e.get("sagaId").toString();

            boolean locked = cartService.lockCart(userId);
            EventType reply = locked ? CartLocked : CartLockFailed;

            List<CartItem> items = cartItemService.getCartItemsByUsername(userId);

            // Converter para lista simples de mapas
            List<Map<String, Object>> itemList = items.stream()
                    .map(i -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("bookId", i.getBookId());
                        m.put("quantity", i.getQuantity());
                        return m;
                    })
                    .toList();

            send(reply, Map.of(
                    "userId", userId,
                    "sagaId", sagaId,
                    "items", itemList
            ));
            return;
        }

        // Handler para limpar carrinho de um user específico
        if (CartClearRequested.name().equals(e.get("eventType"))) {
            String userId = e.get("userId").toString();
            String sagaId = e.containsKey("sagaId") ? e.get("sagaId").toString() : null;
            cartItemService.clearCartForUser(userId);
            // Inclui sagaId se existir
            if (sagaId != null) {
                send(CartCleared, Map.of("userId", userId, "sagaId", sagaId));
            } else {
                send(CartCleared, Map.of("userId", userId));
            }
            return;
        }
    }

    private void send(EventType type, Map<String,Object> body) throws Exception {
        var m = new java.util.HashMap<>(body);
        m.put("eventType", type.name());
        kafka.send(SagaConstants.TOPIC, mapper.writeValueAsString(m));
    }
}