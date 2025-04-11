
package com.projeto.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
// Remova o import estático não utilizado se não usar withDefaults
// import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity // MUITO IMPORTANTE: Ativa a configuração de segurança do Spring para WebFlux
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        System.out.println("⚙️ Configurando SecurityWebFilterChain da Gateway..."); // Log para confirmar

        http
                // 1. DESATIVAR CSRF: Essencial para APIs stateless (como JWT) e para permitir POST em /auth/login
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 2. AUTORIZAÇÃO: Como a lógica de autenticação JWT está no seu filtro AuthenticationFilter
                //    e nos serviços downstream, podemos permitir tudo aqui na camada base do Spring Security da Gateway.
                //    O seu AuthenticationFilter tratará da proteção das rotas não-/auth/**.
                .authorizeExchange(exchange -> exchange
                                .pathMatchers("/auth/**").permitAll() // Permite /auth/** explicitamente (boa prática)
                                .anyExchange().permitAll() // Permite tudo o resto NESTA CAMADA. O AuthenticationFilter agirá depois.
                        // Se tivesse actuator ou outras rotas específicas da gateway para proteger, poderia fazê-lo aqui.
                )

                // 3. DESATIVAR OUTROS MECANISMOS: Não precisamos de autenticação básica ou formulário de login na Gateway
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable);


        return http.build();
    }
}

