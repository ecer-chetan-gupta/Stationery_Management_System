package com.sms.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security Config for API Gateway.
 *
 * The Gateway's security is intentionally permissive at the Spring Security level —
 * JWT validation is handled by our custom JwtAuthFilter (a GatewayFilter), not here.
 *
 * We disable CSRF (stateless JWT) and let all requests through Spring Security,
 * relying on the JwtAuthFilter in the route pipeline for actual auth enforcement.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // disable csrf since api-gateway routes stateless api requests carrying jwt tokens
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // permit all requests at Spring Security level; actual auth is checked via JwtAuthFilter in the route definition
                        .anyExchange().permitAll()
                )
                .build();
    }
}
