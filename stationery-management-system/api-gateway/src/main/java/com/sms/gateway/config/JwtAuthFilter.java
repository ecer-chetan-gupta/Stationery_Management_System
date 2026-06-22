package com.sms.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Authentication Filter for the API Gateway.
 *
 * This reactive gateway filter intercepts every request:
 *   - If the path starts with /api/auth/** -> passes through without checking JWT (public routes)
 *   - All other paths -> validates Bearer token from Authorization header
 *   - On valid token -> extracts email and role, adds them as X-Auth-* headers for downstream services
 *   - On missing/invalid/expired token -> returns 401 Unauthorized
 *
 * The JWT secret MUST match the secret used in auth-service JwtUtil.
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Paths that are publicly accessible (no JWT required)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Allow public paths through without JWT
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Extract Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            try {
                // Parse and validate the JWT
                Claims claims = parseToken(token);

                // Propagate user info to downstream services via custom headers
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Auth-User-Email", claims.getSubject())
                        .header("X-Auth-User-Role", claims.get("role", String.class))
                        .header("X-Auth-User-Id", String.valueOf(claims.get("userId")))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired for path: {}", path);
                return unauthorizedResponse(exchange, "Token has expired");
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
                log.warn("Invalid JWT token for path: {}: {}", path, e.getMessage());
                return unauthorizedResponse(exchange, "Invalid token");
            } catch (Exception e) {
                log.error("JWT validation error for path: {}: {}", path, e.getMessage());
                return unauthorizedResponse(exchange, "Token validation failed");
            }
        };
    }

    /**
     * Parse the JWT token and return its claims.
     * Uses the shared JWT secret (must match auth-service).
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if the request path is a public endpoint (no auth required).
     * Only register and login are truly public — /me, /validate, etc. need JWT.
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Build a 401 Unauthorized response.
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        var buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Config class required by AbstractGatewayFilterFactory.
     * Add fields here if you want to configure the filter per-route in application.yml.
     */
    public static class Config {
        // Can be extended with per-route config options if needed
    }
}
