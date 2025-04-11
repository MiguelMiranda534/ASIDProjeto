package com.projeto.gatewayservice.config;

import com.projeto.gatewayservice.filter.AuthenticationFilter;
import com.projeto.gatewayservice.security.jwt.JwtUtils;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    /**
     * Define o AuthenticationFilter como um Bean GlobalFilter.
     * O Spring injetará automaticamente a dependência JwtUtils
     * porque JwtUtils está anotado com @Component.
     *
     * @param jwtUtils A instância do JwtUtils para validar os tokens.
     * @return Uma instância do AuthenticationFilter configurada como filtro global.
     */
    @Bean
    public GlobalFilter authenticationFilter(JwtUtils jwtUtils) {
        System.out.println("☕️ Registando AuthenticationFilter Bean..."); // Log para confirmar
        return new AuthenticationFilter(jwtUtils);
    }
}