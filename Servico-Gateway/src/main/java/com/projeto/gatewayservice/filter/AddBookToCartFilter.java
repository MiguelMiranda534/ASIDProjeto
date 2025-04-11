package com.projeto.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.Ordered;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Component
public class AddBookToCartFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AddBookToCartFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getURI().getPath().equals("/gateway/addBookToCart")) {

            String bookId = exchange.getRequest().getQueryParams().getFirst("bookId");
            int quantity = Optional.ofNullable(exchange.getRequest().getQueryParams().getFirst("quantity"))
                    .map(q -> {
                        try { return Integer.parseInt(q); }
                        catch (NumberFormatException e) { return 1; }
                    })
                    .filter(q -> q > 0)
                    .orElse(1);

            if (bookId == null || bookId.isEmpty()) {
                return writeErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Parâmetro 'bookId' é obrigatório.");
            }

            String username = exchange.getRequest().getHeaders().getFirst("X-User-Name");
            if (username == null || username.isEmpty()) {
                return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Username não encontrado no header X-User-Name.");
            }

            System.out.println("👤 Utilizador '" + username + "' a adicionar livro " + bookId + " quantidade " + quantity);

            Mono<Map<String, Object>> bookDetailsMono = webClient.get()
                    .uri("lb://servico-catalogo/catalogo/books/{id}", bookId)
                    .retrieve()
                    .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
                            response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro com ID " + bookId + " não encontrado no catálogo.")))
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new ResponseStatusException(response.statusCode(), "Erro ao chamar catálogo: " + body))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});

            return bookDetailsMono.flatMap(bookDetails -> {
                        Object priceObj = bookDetails.get("price");
                        Double unitPrice;

                        if (priceObj instanceof Number) {
                            unitPrice = ((Number) priceObj).doubleValue();
                        } else {
                            System.err.println("❌ Erro: Preço ('price') não encontrado ou inválido nos detalhes do livro " + bookId);
                            return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível obter o preço do livro.");
                        }

                        Map<String, Object> addBookRequest = new HashMap<>();
                        addBookRequest.put("bookId", bookId);
                        addBookRequest.put("userId", username);
                        addBookRequest.put("quantity", quantity);
                        addBookRequest.put("unitPrice", unitPrice);
                        addBookRequest.put("subtotal", unitPrice * quantity);

                        System.out.println("➡️ Enviando para o carrinho: " + addBookRequest);

                        return webClient.post()
                                .uri("lb://servico-carrinho/cart/cartitem/add")
                                .bodyValue(addBookRequest)
                                .retrieve()
                                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                        response -> response.bodyToMono(String.class)
                                                .flatMap(body -> Mono.error(new ResponseStatusException(response.statusCode(), "Erro ao chamar carrinho: " + body))))
                                .bodyToMono(String.class)
                                .flatMap(cartResponse -> {
                                    System.out.println("✅ Resposta do carrinho: " + cartResponse);
                                    Map<String, Object> aggregated = new HashMap<>();
                                    aggregated.put("message", "Livro adicionado com sucesso");
                                    try {
                                        Object parsedCartResponse = objectMapper.readValue(cartResponse, Object.class);
                                        aggregated.put("cartResponse", parsedCartResponse);
                                    } catch (JsonProcessingException jsonEx) {
                                        aggregated.put("cartResponse", cartResponse);
                                    }
                                    return writeSuccessResponse(exchange, aggregated);
                                });

                    })
                    .onErrorResume(ResponseStatusException.class, ex -> {
                        HttpStatus status = (ex.getStatusCode() instanceof HttpStatus)
                                ? (HttpStatus) ex.getStatusCode()
                                : HttpStatus.resolve(ex.getStatusCode().value());

                        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

                        return writeErrorResponse(exchange, status, ex.getReason());
                    })

                    .onErrorResume(Exception.class, ex -> {
                        System.err.println("❌ Erro inesperado no filtro AddBookToCart: " + ex.getMessage());
                        return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no gateway.");
                    });
        }
        return chain.filter(exchange);
    }

    private Mono<Void> writeSuccessResponse(ServerWebExchange exchange, Map<String, Object> responseMap) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseMap);
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erro ao serializar resposta de sucesso: " + e.getMessage());
            return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao formar resposta JSON.");
        }
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        try {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("timestamp", java.time.Instant.now().toString());
            errorMap.put("status", status.value());
            errorMap.put("error", status.getReasonPhrase());
            errorMap.put("message", message);
            errorMap.put("path", exchange.getRequest().getPath().toString());

            byte[] bytes = objectMapper.writeValueAsBytes(errorMap);
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer)).then(Mono.defer(exchange.getResponse()::setComplete));
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erro ao serializar resposta de ERRO: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
