package com.sms.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Config for Auth Service.
 *
 * The API Gateway (JwtAuthFilter) is the single enforcement point for JWT.
 * The auth-service only needs:
 *   - CSRF disabled (stateless JWT)
 *   - Stateless sessions
 *   - Permit all — Gateway guarantees only authenticated requests reach here
 *     (except /register and /login which are public at Gateway level too)
 *
 * We keep BCryptPasswordEncoder as a bean (used by AuthServiceImpl).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // csrf disabled because we use stateless jwt tokens (no session cookies, so no csrf risk)
                .csrf(AbstractHttpConfigurer::disable)
                // tell spring security not to manage session states on the server side
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // let all requests through; the api-gateway filters jwt and forwards role headers
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // bcrypt hashes passwords securely using a random salt internally
        return new BCryptPasswordEncoder();
    }
}
