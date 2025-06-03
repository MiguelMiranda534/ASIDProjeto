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

    // Guarda para cada saga os livros do carrinho (bookId)
    private final Map<String, List<Long>> sagaItems = new HashMap<>();
    // Guarda para cada saga os livros que conseguiram reservar stock
    private final Map<String, Set<Long>> sagaStockOk = new HashMap<>();
    // Guarda para cada saga os livros que falharam reserva
    private final Map<String, Set<Long>> sagaStockFail = new HashMap<>();

    @Autowired
    public CheckoutOrchestrator(ObjectMapper mapper,
                                KafkaTemplate<String, String> kafka,
                                WebClient.Builder webClientBuilder) {
        this.mapper = mapper;
        this.kafka  = kafka;
        this.webClientBuilder = webClientBuilder;
    }

    // Entrada HTTP (POST /checkout/{userId})
    public void startSaga(Long userId) throws Exception {
        String sagaId = UUID.randomUUID().toString();
        publish(Map.of(
                "eventType", CartLockRequested.name(),
                "sagaId",    sagaId,
                "userId",    userId
        ));
        // resto do fluxo reativo
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
            // ignorar eventos de input
            case StockReserveRequested, CartLockRequested, StockReleaseRequested, CartClearRequested -> {}
            default -> {}
        }
    }

    // Handler: Cart Locked
    private void onCartLocked(Map<String, Object> evt) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long userId   = getLong(evt, "userId");

        // Buscar items do carrinho (depois de lock!)
        List<?> cart = webClientBuilder.build()
                .get()
                .uri("lb://servico-carrinho/cart/cartitem/user/{user}", userId)
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .block();

        List<Long> bookIds = new ArrayList<>();
        for (Object c : cart) {
            Map<?,?> ci = (Map<?,?>) c;
            Long bookId = Long.valueOf(ci.get("bookId").toString());
            bookIds.add(bookId);
            // Pedir reserva de stock para cada item
            publish(Map.of(
                    "eventType", StockReserveRequested.name(),
                    "sagaId",    sagaId,
                    "userId",    userId,
                    "bookId",    ci.get("bookId"),
                    "quantity",  ci.get("quantity"),
                    "orderId",   sagaId
            ));
        }
        sagaItems.put(sagaId, bookIds);
        sagaStockOk.put(sagaId, new HashSet<>());
        sagaStockFail.put(sagaId, new HashSet<>());
    }

    // Handler: StockReserved ou StockReserveFailed (para cada item)
    private void onStockReserved(Map<String, Object> evt, boolean failed) throws Exception {
        String sagaId = evt.get("sagaId").toString();
        Long   bookId = getLong(evt, "bookId");
        if (!sagaItems.containsKey(sagaId)) {
            // Pode acontecer se a saga for cancelada antes de terminar todos os eventos
            System.out.println("⚠️  Evento StockReserved para sagaId desconhecido: " + sagaId);
            return;
        }
        // Adiciona ao set de OK/FAIL consoante sucesso
        if (failed) {
            sagaStockFail.get(sagaId).add(bookId);
        } else {
            sagaStockOk.get(sagaId).add(bookId);
        }
        // Só avançar quando TODOS responderam (OK ou FAIL)
        Set<Long> all = new HashSet<>(sagaStockOk.get(sagaId));
        all.addAll(sagaStockFail.get(sagaId));
        if (all.size() == sagaItems.get(sagaId).size()) {
            if (sagaStockFail.get(sagaId).isEmpty()) {
                // Todos com sucesso
                Long userId = getLong(evt, "userId");
                Map<?,?> so = webClientBuilder.build()
                        .post()
                        .uri("lb://servico-shipping/order/shipping")
                        .bodyValue(Map.of("userId", userId))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                Long orderId = Long.valueOf(so.get("id").toString());
                publish(Map.of(
                        "eventType", OrderFinalizeRequested.name(),
                        "sagaId",    sagaId,
                        "orderId",   orderId,
                        "userId",    userId));
            } else {
                // Algum falhou, faz compensação!
                cancelSaga(evt, "STOCK_INSUFICIENTE");
            }
            // Limpar memória (evitar leak)
            sagaItems.remove(sagaId);
            sagaStockOk.remove(sagaId);
            sagaStockFail.remove(sagaId);
        }
    }

    private void onOrderFinalized(Map<String,Object> evt) throws Exception {
        publish(Map.of(
                "eventType", CartClearRequested.name(),
                "sagaId",    evt.get("sagaId"),
                "userId",    evt.get("userId")));
    }

    // Compensação: liberta stock e limpa carrinho
    private void cancelSaga(Map<String,Object> evt, String motivo) throws Exception {
        System.out.println("⚠️  Cancelar saga (" + motivo + ")");
        publish(Map.of(
                "eventType", StockReleaseRequested.name(),
                "sagaId",    evt.get("sagaId"),
                "orderId",   evt.get("orderId")));
        publish(Map.of(
                "eventType", CartClearRequested.name(),
                "sagaId",    evt.get("sagaId"),
                "userId",    evt.get("userId")));
        // Limpar memória
        String sagaId = evt.get("sagaId").toString();
        sagaItems.remove(sagaId);
        sagaStockOk.remove(sagaId);
        sagaStockFail.remove(sagaId);
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