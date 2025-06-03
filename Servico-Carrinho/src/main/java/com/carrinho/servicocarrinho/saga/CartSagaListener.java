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

    @KafkaListener(topics = TOPIC, groupId = "carrinho-saga")
    public void listen(String msg) throws Exception {
        Map<String,Object> e = mapper.readValue(msg, Map.class);

        if (CartLockRequested.name().equals(e.get("eventType"))) {
            Long userId = Long.valueOf(e.get("userId").toString());
            String sagaId = e.get("sagaId").toString();
            System.out.println("🔐 [CartSagaListener] tentando lockCartByUserId → userId=" + userId + ", sagaId=" + sagaId);

            boolean locked = cartService.lockCartByUserId(userId);
            EventType reply = locked ? CartLocked : CartLockFailed;

            List<CartItem> items = cartItemService.getCartItemsByUserId(userId);
            System.out.println("📋 [CartSagaListener] itens no carrinho para userId=" + userId + " → " + items);
            List<Map<String, Object>> itemList = items.stream()
                    .map(i -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("bookId", i.getBookId());
                        m.put("quantity", i.getQuantity());
                        m.put("unitPrice", i.getUnitPrice());
                        m.put("subTotal", i.getSubTotal());
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

        if (CartClearRequested.name().equals(e.get("eventType"))) {
            Long userId = Long.valueOf(e.get("userId").toString());
            String sagaId = e.containsKey("sagaId") ? e.get("sagaId").toString() : null;
            System.out.println("🧹 [CartSagaListener] CartClearRequested → userId=" + userId + ", sagaId=" + sagaId);

            // 1) Apaga todos os CartItem
            cartItemService.clearCartForUserId(userId);

            // 2) "Desbloqueia" o Cart
            cartService.unlockCartByUserId(userId);

            // 3) Responde que o carrinho foi limpo
            if (sagaId != null) {
                send(CartCleared, Map.of("userId", userId, "sagaId", sagaId));
            } else {
                send(CartCleared, Map.of("userId", userId));
            }
        }
    }

    private void send(EventType type, Map<String,Object> body) throws Exception {
        var m = new java.util.HashMap<>(body);
        m.put("eventType", type.name());
        kafka.send(TOPIC, mapper.writeValueAsString(m));
    }
}