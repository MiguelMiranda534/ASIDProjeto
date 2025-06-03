package com.example.servicosaga.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

import static com.example.servicosaga.saga.EventType.*;
import static com.example.servicosaga.saga.SagaConstants.TOPIC;

@Service
public class CheckoutOrchestrator {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafka;
    private final WebClient.Builder webClientBuilder;

    // Guarda para cada saga os itens do carrinho (bookId, quantity, unitPrice, subTotal)
    private final Map<String, List<Map<String, Object>>> sagaItems = new HashMap<>();
    // Guarda para cada saga os livros que conseguiram reservar stock
    private final Map<String, Set<Long>> sagaStockOk = new HashMap<>();
    // Guarda para cada saga os livros que falharam reserva
    private final Map<String, Set<Long>> sagaStockFail = new HashMap<>();
    // Guarda para cada saga o userId (pois o evento de lock já traz o userId)
    private final Map<String, Long> sagaUser = new HashMap<>();

    @Autowired
    public CheckoutOrchestrator(ObjectMapper mapper,
                                KafkaTemplate<String, String> kafka,
                                WebClient.Builder webClientBuilder) {
        this.mapper = mapper;
        this.kafka  = kafka;
        this.webClientBuilder = webClientBuilder;
    }

    public void startSaga(Long userId) throws Exception {
        String sagaId = UUID.randomUUID().toString();
        publish(Map.of(
                "eventType", CartLockRequested.name(),
                "sagaId",    sagaId,
                "userId",    userId
        ));
    }

    @KafkaListener(topics = TOPIC, groupId = "saga-orchestrator-group")
    public void listen(ConsumerRecord<String, String> rec) throws Exception {
        Map<String, Object> evt = mapper.readValue(rec.value(), Map.class);
        if (!evt.containsKey("eventType")) {
            System.out.println("⚠️  Evento sem eventType: " + rec.value());
            return;
        }
        EventType type = EventType.valueOf(evt.get("eventType").toString());

        switch (type) {
            case CartLocked -> onCartLocked(evt);
            case CartLockFailed -> cancelSaga(evt, "LOCK_FAILED");
            case StockReserved -> onStockReserved(evt, false);
            case StockReserveFailed -> onStockReserved(evt, true);
            case OrderFinalized -> onOrderFinalized(evt);
            case OrderFinalizeFailed -> cancelSaga(evt, "FALHA_FINALIZAR");
            // Ignora outros aqui
            case StockReserveRequested, CartLockRequested, StockReleaseRequested, CartClearRequested -> {}
            default -> {}
        }
    }

    private void onCartLocked(Map<String, Object> evt) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long userId   = getLong(evt, "userId");

        // Armazena o userId no contexto da saga
        sagaUser.put(sagaId, userId);

        // Buscar itens do carrinho
        List<?> cart = webClientBuilder.build()
                .get()
                .uri("lb://servico-carrinho/cart/cartitem/user/{user}", userId)
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .block();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object c : cart) {
            Map<?,?> ci = (Map<?,?>) c;
            Long bookId = Long.valueOf(ci.get("bookId").toString());
            Integer quantity = Integer.valueOf(ci.get("quantity").toString());
            Double unitPrice = Double.valueOf(ci.get("unitPrice").toString());
            Double subTotal = Double.valueOf(ci.get("subTotal").toString());
            items.add(Map.of(
                    "bookId", bookId,
                    "quantity", quantity,
                    "unitPrice", unitPrice,
                    "subTotal", subTotal
            ));
            // Para cada item, pede reserva de stock
            publish(Map.of(
                    "eventType", StockReserveRequested.name(),
                    "sagaId",    sagaId,
                    "userId",    userId,
                    "bookId",    bookId,
                    "quantity",  quantity
            ));
        }
        sagaItems.put(sagaId, items);
        sagaStockOk.put(sagaId, new HashSet<>());
        sagaStockFail.put(sagaId, new HashSet<>());
    }

    private void onStockReserved(Map<String, Object> evt, boolean failed) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long bookId = getLong(evt, "bookId");

        if (!sagaItems.containsKey(sagaId)) {
            System.out.println("⚠️  Evento StockReserved para sagaId desconhecido: " + sagaId);
            return;
        }
        if (failed) {
            sagaStockFail.get(sagaId).add(bookId);
        } else {
            sagaStockOk.get(sagaId).add(bookId);
        }

        Set<Long> all = new HashSet<>(sagaStockOk.get(sagaId));
        all.addAll(sagaStockFail.get(sagaId));

        // Se já recebeu resposta para todos os itens:
        if (all.size() == sagaItems.get(sagaId).size()) {
            // Se não houve falhas de stock:
            if (sagaStockFail.get(sagaId).isEmpty()) {
                Long userId = sagaUser.get(sagaId); // recupera o userId armazenado
                // 1) Criar ShippingOrder
                Map<String, Object> shippingOrder = Map.of(
                        "userId",    userId,
                        "firstName", "Default",
                        "lastName",  "User",
                        "address",   "Default Address",
                        "city",      "Default City",
                        "email",     "default@example.com",
                        "postal_code","12345"
                );
                Map<?,?> so = webClientBuilder.build()
                        .post()
                        .uri("lb://servico-shipping/order/shipping")
                        .bodyValue(shippingOrder)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                Long orderId = Long.valueOf(so.get("id").toString());

                // 2) Criar OrderDetails para cada item
                for (Map<String, Object> item : sagaItems.get(sagaId)) {
                    Map<String, Object> orderDetail = Map.of(
                            "quantity",       item.get("quantity"),
                            "subTotal",       item.get("subTotal"),
                            "bookId",         item.get("bookId"),
                            "shippingOrderId",orderId,
                            "userId",         userId
                    );
                    webClientBuilder.build()
                            .post()
                            .uri("lb://servico-shipping/order/details")
                            .bodyValue(orderDetail)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                }

                // 3) Solicita finalização de pedido
                publish(Map.of(
                        "eventType", OrderFinalizeRequested.name(),
                        "sagaId",    sagaId,
                        "orderId",   orderId,
                        "userId",    userId
                ));
            } else {
                // Se houve falha de stock, cancela saga
                cancelSaga(evt, "STOCK_INSUFICIENTE");
            }

            // Limpar contexto
            sagaItems.remove(sagaId);
            sagaStockOk.remove(sagaId);
            sagaStockFail.remove(sagaId);
            sagaUser.remove(sagaId);
        }
    }

    private void onOrderFinalized(Map<String,Object> evt) throws Exception {
        // Quando o pedido estiver fechado, limpa o carrinho
        publish(Map.of(
                "eventType", CartClearRequested.name(),
                "sagaId",    evt.get("sagaId"),
                "userId",    evt.get("userId")
        ));
    }

    private void cancelSaga(Map<String,Object> evt, String motivo) throws Exception {
        System.out.println("⚠️  Cancelar saga (" + motivo + ")");
        String sagaId = evt.get("sagaId").toString();

        // Se ainda houver itens, libera stock
        if (sagaItems.containsKey(sagaId)) {
            for (Map<String, Object> item : sagaItems.get(sagaId)) {
                publish(Map.of(
                        "eventType", StockReleaseRequested.name(),
                        "sagaId",    sagaId,
                        "bookId",    item.get("bookId"),
                        "quantity",  item.get("quantity")
                ));
            }
        }
        // Limpa o carrinho mesmo no cancelamento
        publish(Map.of(
                "eventType", CartClearRequested.name(),
                "sagaId",    sagaId,
                "userId",    evt.get("userId")
        ));

        // Limpa contexto
        sagaItems.remove(sagaId);
        sagaStockOk.remove(sagaId);
        sagaStockFail.remove(sagaId);
        sagaUser.remove(sagaId);
    }

    private void publish(Map<String, Object> body) throws Exception {
        kafka.send(TOPIC, mapper.writeValueAsString(body));
        System.out.println("📤 SAGA envia: " + body);
    }

    private Long getLong(Map<String,Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && !s.isBlank()) return Long.valueOf(s);
        return null;
    }
}