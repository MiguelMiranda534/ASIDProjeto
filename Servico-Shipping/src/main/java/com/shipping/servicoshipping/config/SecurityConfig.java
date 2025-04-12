package com.shipping.servicoshipping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desativa CSRF (com a nova sintaxe)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite todas as requests sem autenticação
                );
        return http.build();
    }
}