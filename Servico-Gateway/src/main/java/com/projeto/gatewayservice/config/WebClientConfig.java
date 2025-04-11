package com.projeto.gatewayservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // <--- isto é essencial para suportar "lb://"
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
