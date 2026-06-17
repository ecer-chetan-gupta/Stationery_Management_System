package com.sms.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for the API Gateway.
 *
 * IMPORTANT: CORS must ONLY be configured here in the Gateway.
 * Individual microservices must NOT add CORS config — it would
 * create duplicate headers and break the browser.
 *
 * In production, replace the allowed origin with your actual frontend domain.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from the React dev server and Docker-served frontend
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",   // Vite dev server
                "http://localhost:80",     // Nginx Docker
                "http://localhost"         // Nginx Docker (port 80 implicit)
        ));

        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers including Authorization (for JWT)
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies / credentials (needed for some auth flows)
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
