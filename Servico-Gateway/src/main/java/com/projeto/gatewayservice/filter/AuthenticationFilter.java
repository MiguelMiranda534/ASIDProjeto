package com.projeto.gatewayservice.filter;

import com.projeto.gatewayservice.security.jwt.JwtUtils;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    public AuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("🔥 Filtro JWT foi chamado!");

        String path = exchange.getRequest().getPath().toString();

        // Permitir login e registo sem token
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta o token JWT");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        String username = jwtUtils.getUsernameFromJwt(token);

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r.headers(h -> h.set("X-User-Name", username)))
                .build();

        return chain.filter(modifiedExchange);
    }
    @Override
    public int getOrder() {
        // Define uma prioridade mais alta (-100) do que AddBookToCartFilter (-1)
        return -100;
    }
}
