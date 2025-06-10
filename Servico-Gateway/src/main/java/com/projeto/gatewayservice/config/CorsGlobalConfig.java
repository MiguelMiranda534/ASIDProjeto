// Servico-Gateway/src/main/java/com/projeto/gatewayservice/config/CorsGlobalConfig.java
package com.projeto.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsGlobalConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 1) Permitir explicitamente a origem do teu frontend:
        corsConfig.setAllowedOrigins(List.of("http://localhost:8000"));

        // 2) Permitir todos os métodos HTTP usados no frontend
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3) Permitir todos os cabeçalhos (especialmente Content-Type e Authorization)
        corsConfig.setAllowedHeaders(List.of("*"));

        // 4) Expor cabeçalhos se for necessário (por exemplo, Authorization, etc.)
        corsConfig.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // 5) Se estiveres a usar cookies ou queres enviar credenciais (não é obrigatório para JWT)
        corsConfig.setAllowCredentials(true);

        // 6) Quanto tempo o navegador pode fazer cache do preflight (em segundos)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Regista para todas as rotas do Gateway
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
