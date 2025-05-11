package com.projeto.gatewayservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.gatewayservice.security.jwt.JwtUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AddBookToCartController {

    private final WebClient webClient;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AddBookToCartController(WebClient.Builder webClientBuilder, JwtUtils jwtUtils) {
        this.webClient = webClientBuilder.build();
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/gateway/addBookToCart")
    public Mono<ResponseEntity<Map<String, Object>>> addBookToCart(
            @RequestParam String bookId,
            @RequestParam(required = false, defaultValue = "1") int quantity,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        if (quantity <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parâmetro 'quantity' deve ser maior que zero."));
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT não fornecido ou mal formatado"));
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtUtils.getUsernameFromJwt(token);
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT inválido"));
        }

        System.out.println("\uD83D\uDC64 Controller: Utilizador '" + username + "' a adicionar livro " + bookId + " quantidade " + quantity);

        Mono<Map<String, Object>> bookDetailsMono = webClient.get()
                .uri("lb://servico-catalogo/catalogo/books/{id}", bookId)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro com ID " + bookId + " não encontrado no catálogo.")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(response.statusCode(), "Erro ao chamar catálogo: " + body))))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnError(e -> System.err.println("❌ Erro ao buscar detalhes do livro " + bookId + ": " + e.getMessage()));

        return bookDetailsMono.flatMap(bookDetails -> {
                    Object priceObj = bookDetails.get("price");
                    Double unitPrice;

                    if (priceObj instanceof Number) {
                        unitPrice = ((Number) priceObj).doubleValue();
                    } else {
                        System.err.println("❌ Erro: Preço ('price') não encontrado ou inválido nos detalhes do livro " + bookId);
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível obter o preço do livro do catálogo."));
                    }

                    Map<String, Object> addBookRequest = new HashMap<>();
                    addBookRequest.put("bookId", bookId);
                    addBookRequest.put("username", username);
                    addBookRequest.put("quantity", quantity);
                    addBookRequest.put("unitPrice", unitPrice);
                    addBookRequest.put("subtotal", unitPrice * quantity);

                    System.out.println("➡️ Enviando para o carrinho: " + addBookRequest);

                    return webClient.post()
                            .uri("lb://servico-carrinho/cart/cartitem/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(addBookRequest)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    response -> response.bodyToMono(String.class)
                                            .flatMap(body -> Mono.error(new ResponseStatusException(response.statusCode(), "Erro ao chamar serviço de carrinho: " + body))))
                            .bodyToMono(String.class)
                            .flatMap(cartResponseString -> {
                                System.out.println("✅ Resposta do carrinho: " + cartResponseString);
                                Map<String, Object> aggregatedResponse = new HashMap<>();
                                aggregatedResponse.put("message", "Livro adicionado com sucesso");
                                try {
                                    Object parsedCartResponse = objectMapper.readValue(cartResponseString, Object.class);
                                    aggregatedResponse.put("cartResponse", parsedCartResponse);
                                } catch (JsonProcessingException jsonEx) {
                                    System.err.println("⚠️ Não foi possível fazer parse da resposta do carrinho como JSON: " + jsonEx.getMessage());
                                    aggregatedResponse.put("cartResponse", cartResponseString);
                                }
                                return Mono.just(ResponseEntity.ok(aggregatedResponse));
                            })
                            .doOnError(e -> System.err.println("❌ Erro ao adicionar ao carrinho: " + e.getMessage()));
                })
                .onErrorResume(ResponseStatusException.class, ex -> {
                    System.err.println("❌ Erro tratado (ResponseStatusException): " + ex.getStatusCode() + " - " + ex.getReason());
                    Map<String, Object> errorAttributes = new HashMap<>();
                    errorAttributes.put("timestamp", java.time.Instant.now().toString());
                    errorAttributes.put("status", ex.getStatusCode().value());
                    errorAttributes.put("error", HttpStatus.resolve(ex.getStatusCode().value()).getReasonPhrase());
                    errorAttributes.put("message", ex.getReason());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorAttributes));
                })
                .onErrorResume(Exception.class, ex -> {
                    System.err.println("❌ Erro inesperado no controller AddBookToCart: " + ex.getMessage());
                    Map<String, Object> errorAttributes = new HashMap<>();
                    errorAttributes.put("timestamp", java.time.Instant.now().toString());
                    errorAttributes.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    errorAttributes.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    errorAttributes.put("message", "Erro interno no servidor ao processar a adição ao carrinho.");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorAttributes));
                });
    }
}
