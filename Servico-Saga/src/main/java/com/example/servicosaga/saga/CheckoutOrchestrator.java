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

    /** Armazena, para cada sagaId, a lista de itens do carrinho. */
    private final Map<String, List<Map<String, Object>>> sagaItems = new HashMap<>();

    /** Para cada sagaId, mantém o número de respostas de stock recebidas. */
    private final Map<String, Integer> sagaStockResponses = new HashMap<>();

    /** Guarda o userId para cada sagaId. */
    private final Map<String, Long> sagaUser = new HashMap<>();

    /** Guarda o shippingOrderId retornado em onStockReserved. */
    private final Map<String, Long> sagaShippingId = new HashMap<>();

    /** Guarda o orderId real, capturado no evento OrderCreated. */
    private final Map<String, Long> sagaOrderId = new HashMap<>();

    @Autowired
    public CheckoutOrchestrator(ObjectMapper mapper,
                                KafkaTemplate<String, String> kafka,
                                WebClient.Builder webClientBuilder) {
        this.mapper = mapper;
        this.kafka = kafka;
        this.webClientBuilder = webClientBuilder;
    }

    public void startSaga(Long userId) throws Exception {
        String sagaId = UUID.randomUUID().toString();
        publish(Map.of(
                "eventType", CartLockRequested.name(),
                "sagaId", sagaId,
                "userId", userId
        ));
    }

    @KafkaListener(topics = TOPIC, groupId = "saga-orchestrator-group")
    public void listen(ConsumerRecord<String, String> rec) throws Exception {
        Map<String, Object> evt = mapper.readValue(rec.value(), Map.class);
        if (!evt.containsKey("eventType")) {
            System.out.println("⚠️ Evento sem eventType: " + rec.value());
            return;
        }
        EventType type = EventType.valueOf(evt.get("eventType").toString());

        switch (type) {
            case CartLocked -> onCartLocked(evt);
            case CartLockFailed -> cancelSaga(evt, "LOCK_FAILED");
            case StockReserved -> onStockReserved(evt, false);
            case StockReserveFailed -> onStockReserved(evt, true);
            case OrderCreated -> onOrderCreated(evt);
            case OrderFinalized -> onOrderFinalized(evt);
            case OrderFinalizeFailed -> cancelSaga(evt, "FALHA_FINALIZAR");
            default -> { /* nada */ }
        }
    }

    private void onCartLocked(Map<String, Object> evt) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long userId = getLong(evt, "userId");

        System.out.println("🔐 [Saga] Carrinho bloqueado para userId=" + userId + ", sagaId=" + sagaId);
        sagaUser.put(sagaId, userId);

        List<?> cart = webClientBuilder.build()
                .get()
                .uri("lb://servico-carrinho/cart/cartitem/user/{user}", userId)
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .block();

        List<Map<String, Object>> items = new ArrayList<>();
        if (cart != null) {
            for (Object c : cart) {
                @SuppressWarnings("unchecked")
                Map<String, ?> ci = (Map<String, ?>) c;
                Long bookId = Long.valueOf(ci.get("bookId").toString());
                Integer qty = Integer.valueOf(ci.get("quantity").toString());
                Double unit = Double.valueOf(ci.get("unitPrice").toString());
                Double sub = Double.valueOf(ci.get("subTotal").toString());

                items.add(Map.of(
                        "bookId", bookId,
                        "quantity", qty,
                        "unitPrice", unit,
                        "subTotal", sub
                ));

                Map<String, Object> reservePayload = Map.of(
                        "eventType", StockReserveRequested.name(),
                        "sagaId", sagaId,
                        "userId", userId,
                        "bookId", bookId,
                        "quantity", qty
                );
                System.out.println("📤 [SagaOrchestrator] publicando StockReserveRequested → " + reservePayload);
                publish(reservePayload);
            }
        }

        sagaItems.put(sagaId, items);
        sagaStockResponses.put(sagaId, 0); // Inicializa contador de respostas

        System.out.println("🛒 [Saga] Enviados StockReserveRequested para " + items.size() + " itens.");
    }

    private void onStockReserved(Map<String, Object> evt, boolean failed) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long bookId = getLong(evt, "bookId");
        Integer quantity = Integer.valueOf(evt.get("quantity").toString());

        System.out.println("🔔 [SagaOrchestrator] onStockReserved chamado → eventType="
                + evt.get("eventType")
                + ", bookId=" + bookId
                + ", quantity=" + quantity
                + ", failed=" + failed
                + ", sagaId=" + sagaId);

        if (!sagaItems.containsKey(sagaId)) {
            System.out.println("⚠️ [SagaOrchestrator] sagaItems não contém sagaId=" + sagaId);
            return;
        }

        // Incrementa o contador de respostas
        sagaStockResponses.compute(sagaId, (k, v) -> (v == null) ? 1 : v + 1);

        if (failed) {
            System.out.println("❌ [SagaOrchestrator] falha ao reservar stock → bookId="
                    + bookId + ", quantity=" + quantity + " (sagaId=" + sagaId + ")");
            cancelSaga(evt, "STOCK_INSUFICIENTE");
            return;
        } else {
            System.out.println("✅ [SagaOrchestrator] stock reservado → bookId="
                    + bookId + ", quantity=" + quantity + " (sagaId=" + sagaId + ")");
        }

        int totalEsperado = sagaItems.get(sagaId).size();
        int totalRecebido = sagaStockResponses.get(sagaId);
        System.out.println("🧮 [SagaOrchestrator] respostas recebidas: "
                + totalRecebido + "/" + totalEsperado
                + " (sagaId=" + sagaId + ")");

        if (totalRecebido == totalEsperado) {
            Long userId = sagaUser.get(sagaId);
            System.out.println("🚚 [SagaOrchestrator] todos reservados OK, a invocar ShippingService para sagaId="
                    + sagaId + ", userId=" + userId);

            Map<String, Object> payload = Map.of(
                    "userId", userId,
                    "firstName", "Default",
                    "lastName", "User",
                    "address", "Default Address",
                    "city", "Default City",
                    "email", "default@example.com",
                    "postal_code", "12345",
                    "sagaId", sagaId
            );
            System.out.println("📤 [SagaOrchestrator] POST /order/shipping body: " + payload);

            Map<?, ?> soResponse = webClientBuilder.build()
                    .post()
                    .uri("lb://servico-shipping/order/shipping")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("📥 [SagaOrchestrator] resposta do ShippingService: " + soResponse);

            if (soResponse != null && soResponse.containsKey("id")) {
                Long shippingId = Long.valueOf(soResponse.get("id").toString());
                sagaShippingId.put(sagaId, shippingId);
                System.out.println("🚚 [SagaOrchestrator] ShippingOrder criado → shippingId="
                        + shippingId + " (sagaId=" + sagaId + ")");
            } else {
                System.out.println("❌ [SagaOrchestrator] falha ao criar ShippingOrder (sagaId=" + sagaId + ")");
                cancelSaga(evt, "ERRO_CRIAR_SHIPPING");
            }

            sagaStockResponses.remove(sagaId); // Limpa o contador
        }
    }

    private void onOrderCreated(Map<String, Object> evt) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long orderId = getLong(evt, "orderId");

        if (sagaItems.containsKey(sagaId) && !sagaOrderId.containsKey(sagaId)) {
            sagaOrderId.put(sagaId, orderId);
            System.out.println("🛅 [Saga] Capturado OrderCreated → orderId=" + orderId + ", sagaId=" + sagaId);

            Long userId = sagaUser.get(sagaId);
            Long shippingOrderId = sagaShippingId.get(sagaId);

            for (Map<String, Object> item : sagaItems.get(sagaId)) {
                Map<String, Object> detailPayload = Map.of(
                        "quantity", item.get("quantity"),
                        "subTotal", item.get("subTotal"),
                        "bookId", item.get("bookId"),
                        "shippingOrderId", shippingOrderId,
                        "userId", userId
                );
                webClientBuilder.build()
                        .post()
                        .uri("lb://servico-shipping/order/details")
                        .bodyValue(detailPayload)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                System.out.println("📦 [Saga] Pedido criar OrderDetails: " + detailPayload);
            }

            publish(Map.of(
                    "eventType", OrderFinalizeRequested.name(),
                    "sagaId", sagaId,
                    "orderId", orderId,
                    "userId", userId
            ));
        }
    }

    private void onOrderFinalized(Map<String, Object> evt) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long userId = getLong(evt, "userId");
        System.out.println("✅ [Saga] Pedido finalizado → orderId=" + evt.get("orderId") + ", sagaId=" + sagaId);

        publish(Map.of(
                "eventType", CartClearRequested.name(),
                "sagaId", sagaId,
                "userId", userId
        ));

        sagaItems.remove(sagaId);
        sagaShippingId.remove(sagaId);
        sagaOrderId.remove(sagaId);
        sagaUser.remove(sagaId);
    }

    private void cancelSaga(Map<String, Object> evt, String motivo) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        System.out.println("⚠️ Cancelar saga (" + motivo + "), sagaId=" + sagaId);

        if (sagaItems.containsKey(sagaId)) {
            for (Map<String, Object> item : sagaItems.get(sagaId)) {
                publish(Map.of(
                        "eventType", StockReleaseRequested.name(),
                        "sagaId", sagaId,
                        "userId", sagaUser.get(sagaId),
                        "bookId", item.get("bookId"),
                        "quantity", item.get("quantity")
                ));
            }
        }

        publish(Map.of(
                "eventType", CartClearRequested.name(),
                "sagaId", sagaId,
                "userId", sagaUser.getOrDefault(sagaId, getLong(evt, "userId"))
        ));

        sagaItems.remove(sagaId);
        sagaStockResponses.remove(sagaId);
        sagaShippingId.remove(sagaId);
        sagaOrderId.remove(sagaId);
        sagaUser.remove(sagaId);
    }

    private void publish(Map<String, Object> body) throws Exception {
        kafka.send(TOPIC, mapper.writeValueAsString(body));
        System.out.println("📤 [Saga envia] " + body);
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String && !((String) value).isBlank()) return Long.valueOf((String) value);
        return null;
    }
}