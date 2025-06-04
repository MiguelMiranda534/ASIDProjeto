// Servico-Saga/src/main/java/com/example/servicosaga/config/WebClientConfig.java
package com.example.servicosaga.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Este bean permite resolver URIs do tipo "lb://servico-carrinho"
    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}