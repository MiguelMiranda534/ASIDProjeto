package com.example.servicosaga.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class CheckoutOrchestrator {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafka;
    private final WebClient.Builder webClientBuilder;

    private final Map<String, List<Map<String, Object>>> sagaItems = new HashMap<>();
    private final Map<String, Integer> sagaStockResponses = new HashMap<>();
    private final Map<String, Long> sagaUser = new HashMap<>();
    private final Map<String, Long> sagaShippingId = new HashMap<>();
    private final Map<String, Long> sagaOrderId = new HashMap<>();
    private final Map<String, Map<String, Object>> sagaShippingDetails = new HashMap<>();
    private final Map<String, String> sagaStatus = new HashMap<>();

    @Autowired
    public CheckoutOrchestrator(ObjectMapper mapper,
                                KafkaTemplate<String, String> kafka,
                                WebClient.Builder webClientBuilder) {
        this.mapper = mapper;
        this.kafka = kafka;
        this.webClientBuilder = webClientBuilder;
    }

    public void startSaga(String sagaId, Long userId, Map<String, Object> shippingDetails) throws Exception {
        sagaStatus.put(sagaId, "IN_PROGRESS");
        sagaShippingDetails.put(sagaId, shippingDetails);
        publish(Map.of(
                "eventType", EventType.CartLockRequested.name(),
                "sagaId", sagaId,
                "userId", userId
        ));
    }

    @KafkaListener(topics = SagaConstants.TOPIC, groupId = "saga-orchestrator-group")
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
                .uri("http://servico-carrinho-lb:8083/cart/cartitem/user/{user}", userId)
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

                Map<String, Object> item = new HashMap<>();
                item.put("bookId", bookId);
                item.put("quantity", qty);
                item.put("unitPrice", unit);
                item.put("subTotal", sub);
                item.put("reserved", false);
                items.add(item);

                Map<String, Object> reservePayload = Map.of(
                        "eventType", EventType.StockReserveRequested.name(),
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
        sagaStockResponses.put(sagaId, 0);

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

        sagaStockResponses.compute(sagaId, (k, v) -> (v == null) ? 1 : v + 1);

        if (!failed) {
            List<Map<String, Object>> items = sagaItems.get(sagaId);
            for (Map<String, Object> item : items) {
                if (item.get("bookId").equals(bookId)) {
                    item.put("reserved", true);
                    break;
                }
            }
            System.out.println("✅ [SagaOrchestrator] stock reservado → bookId="
                    + bookId + ", quantity=" + quantity + " (sagaId=" + sagaId + ")");
        } else {
            System.out.println("❌ [SagaOrchestrator] falha ao reservar stock → bookId="
                    + bookId + ", quantity=" + quantity + " (sagaId=" + sagaId + ")");
            cancelSaga(evt, "STOCK_INSUFICIENTE");
            return;
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

            Map<String, Object> userShippingDetails = sagaShippingDetails.get(sagaId);
            if (userShippingDetails != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                payload.put("firstName", userShippingDetails.get("firstName"));
                payload.put("lastName", userShippingDetails.get("lastName"));
                payload.put("address", userShippingDetails.get("address"));
                payload.put("city", userShippingDetails.get("city"));
                payload.put("email", userShippingDetails.get("email"));
                payload.put("postal_code", userShippingDetails.get("postal_code"));
                payload.put("sagaId", sagaId);

                System.out.println("📤 [SagaOrchestrator] POST /order/shipping body: " + payload);

                Map<?, ?> soResponse = webClientBuilder.build()
                        .post()
                        .uri("http://servico-shipping-lb:8084/order/shipping")
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

                sagaStockResponses.remove(sagaId);
            } else {
                throw new RuntimeException("Detalhes de envio não encontrados para sagaId: " + sagaId);
            }
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
                        .uri("http://servico-shipping-lb:8084/order/details")
                        .bodyValue(detailPayload)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                System.out.println("📦 [Saga] Pedido criar OrderDetails: " + detailPayload);
            }

            publish(Map.of(
                    "eventType", EventType.OrderFinalizeRequested.name(),
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
                "eventType", EventType.CartClearRequested.name(),
                "sagaId", sagaId,
                "userId", userId
        ));
        sagaStatus.put(sagaId, "SUCCESS"); // Marcar como sucesso
        // Limpar mapas
        sagaItems.remove(sagaId);
        sagaShippingId.remove(sagaId);
        sagaOrderId.remove(sagaId);
        sagaUser.remove(sagaId);
        sagaShippingDetails.remove(sagaId);
    }

    private void cancelSaga(Map<String, Object> evt, String motivo) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        System.out.println("⚠️ Cancelar saga (" + motivo + "), sagaId=" + sagaId);

        if (sagaItems.containsKey(sagaId)) {
            for (Map<String, Object> item : sagaItems.get(sagaId)) {
                if ((boolean) item.getOrDefault("reserved", false)) {
                    publish(Map.of(
                            "eventType", EventType.StockReleaseRequested.name(),
                            "sagaId", sagaId,
                            "userId", sagaUser.get(sagaId),
                            "bookId", item.get("bookId"),
                            "quantity", item.get("quantity")
                    ));
                }
            }
        }

        publish(Map.of(
                "eventType", EventType.CartClearRequested.name(),
                "sagaId", sagaId,
                "userId", sagaUser.getOrDefault(sagaId, getLong(evt, "userId"))
        ));
        sagaStatus.put(sagaId, "FAILED"); // Marcar como falha
        sagaItems.remove(sagaId);
        sagaStockResponses.remove(sagaId);
        sagaShippingId.remove(sagaId);
        sagaOrderId.remove(sagaId);
        sagaUser.remove(sagaId);
        sagaShippingDetails.remove(sagaId);
    }

    private void publish(Map<String, Object> body) throws Exception {
        kafka.send(SagaConstants.TOPIC, mapper.writeValueAsString(body));
        System.out.println("📤 [Saga envia] " + body);
    }

    public String getSagaStatus(String sagaId) {
        return sagaStatus.getOrDefault(sagaId, "UNKNOWN");
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String && !((String) value).isBlank()) return Long.valueOf((String) value);
        return null;
    }
}