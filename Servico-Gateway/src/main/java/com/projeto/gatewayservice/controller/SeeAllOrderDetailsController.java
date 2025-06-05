package com.projeto.gatewayservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.gatewayservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SeeAllOrderDetailsController {

    private final WebClient.Builder webClientBuilder;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    @Autowired
    public SeeAllOrderDetailsController(WebClient.Builder webClientBuilder, JwtUtils jwtUtils) {
        this.webClientBuilder = webClientBuilder;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/gateway/orderDetails/{shippingOrderId}")
    public Mono<ResponseEntity<Map<String, Object>>> getComposedOrderDetails(
            @PathVariable Long shippingOrderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        // 1) validação básica do JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ") ||
                !jwtUtils.validateJwtToken(authHeader.substring(7))) {
            return Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(createErrorMap(HttpStatus.UNAUTHORIZED, "Token inválido ou ausente"))
            );
        }

        // 2) busca todos os OrderDetails daquele shippingOrderId
        Mono<List<Map<String, Object>>> detailsMono = webClientBuilder.build().get()
                .uri("http://servico-shipping:8084/order/details/shipping/{id}", shippingOrderId)
                .retrieve()
                .bodyToMono(LIST_MAP_TYPE_REFERENCE)
                .onErrorResume(WebClientResponseException.NotFound.class,
                        ex -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "OrderDetails não encontrados: " + shippingOrderId)))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "Erro ao buscar OrderDetails: " + ex.getStatusCode())));

        return detailsMono.flatMap(orderDetailsList -> {
                    if (orderDetailsList.isEmpty()) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(createErrorMap(HttpStatus.NOT_FOUND,
                                        "Nenhum OrderDetails encontrado para shippingOrderId " + shippingOrderId)));
                    }

                    // 3) busca o ShippingOrder para extrair o userId corretamente
                    Mono<Map<String, Object>> shippingMono = getShippingDetails(shippingOrderId);

                    // 4) quando tivermos o ShippingOrder, extraímos o userId e chamamos o Auth
                    Mono<Map<String, Object>> userMono = shippingMono.flatMap(shipping -> {
                        Long userId = getLongFromMap(shipping, "userId");
                        if (userId == null) {
                            // caso improvável, mas protegemos: devolve um Map com erro
                            return Mono.just(Map.of("error", "userId ausente no ShippingOrder", "id", shippingOrderId));
                        }
                        return getUserDetails(userId);
                    });

                    // 5) busca cada detalhe de livro em paralelo (seja para mostrar preço, título etc.)
                    List<Mono<Map<String, Object>>> bookMonos = orderDetailsList.stream()
                            .map(item -> getBookDetails(getLongFromMap(item, "bookId")))
                            .collect(Collectors.toList());
                    Mono<List<Map<String, Object>>> allBooksMono = Flux.concat(bookMonos).collectList();

                    // 6) quando tivermos userMono, shippingMono e allBooksMono, montamos a resposta final
                    return Mono.zip(userMono, shippingMono, allBooksMono)
                            .map(tuple -> {
                                Map<String, Object> user = tuple.getT1();
                                Map<String, Object> shipping = tuple.getT2();
                                List<Map<String, Object>> books = tuple.getT3();

                                // cria um mapa bookId → detalhes do livro
                                Map<Long, Map<String, Object>> bookMap = books.stream()
                                        .filter(b -> b != null && b.get("id") != null)
                                        .collect(Collectors.toMap(
                                                b -> getLongFromMap(b, "id"),
                                                b -> b,
                                                (existente, substituto) -> existente
                                        ));

                                // monta a lista definitiva de items
                                List<Map<String, Object>> items = orderDetailsList.stream()
                                        .map(detail -> {
                                            Map<String, Object> item = new HashMap<>();
                                            item.put("quantity", detail.get("quantity"));
                                            item.put("subTotal", detail.get("subTotal"));
                                            Long bookId = getLongFromMap(detail, "bookId");
                                            item.put("bookDetails",
                                                    bookMap.getOrDefault(
                                                            bookId,
                                                            Map.of("error", "Livro não encontrado", "id", bookId)
                                                    )
                                            );
                                            return item;
                                        }).collect(Collectors.toList());

                                // monta o JSON de resposta
                                Map<String, Object> response = new HashMap<>();
                                response.put("userDetails", filterUserDetails(user));
                                response.put("shippingDetails", shipping);
                                response.put("items", items);
                                return ResponseEntity.ok(response);
                            });
                })
                .onErrorResume(ResponseStatusException.class, ex ->
                        Mono.just(
                                ResponseEntity.status(ex.getStatusCode())
                                        .body(createErrorMap((HttpStatus) ex.getStatusCode(), ex.getReason()))
                        ))
                .onErrorResume(Exception.class, ex -> {
                    ex.printStackTrace();
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(createErrorMap(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor."))
                    );
                });
    }

    private Mono<Map<String, Object>> getShippingDetails(Long id) {
        return webClientBuilder.build().get()
                .uri("http://servico-shipping:8084/order/shipping/{id}", id)
                .retrieve()
                .bodyToMono(MAP_TYPE_REFERENCE)
                .onErrorResume(WebClientResponseException.NotFound.class,
                        ex -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "ShippingOrder não encontrado: " + id)))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.just(Map.of("error", "Erro ao buscar envio", "id", id)));
    }

    private Mono<Map<String, Object>> getUserDetails(Long id) {
        return webClientBuilder.build().get()
                .uri("http://servico-auth:8081/auth/users/{id}", id)
                .retrieve()
                .bodyToMono(MAP_TYPE_REFERENCE)
                .onErrorResume(WebClientResponseException.NotFound.class,
                        ex -> Mono.just(Map.of("error", "Usuário não encontrado", "id", id)))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.just(Map.of("error", "Erro ao buscar utilizador", "id", id)));
    }

    private Map<String, Object> filterUserDetails(Map<String, Object> user) {
        Map<String, Object> filtered = new HashMap<>();
        filtered.put("id", user.get("id"));
        filtered.put("fullname", user.get("fullname"));
        filtered.put("username", user.get("username"));
        filtered.put("email", user.get("email"));
        return filtered;
    }

    private Mono<Map<String, Object>> getBookDetails(Long id) {
        return webClientBuilder.build().get()
                .uri("http://servico-catalogo:8082/catalogo/books/{id}", id)
                .retrieve()
                .bodyToMono(MAP_TYPE_REFERENCE)
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.just(Map.of("error", "Erro ao buscar livro", "id", id)));
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) {
            try { return Long.parseLong((String) val); }
            catch (Exception ignored) {}
        }
        return null;
    }

    private Map<String, Object> createErrorMap(HttpStatus status, String message) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
