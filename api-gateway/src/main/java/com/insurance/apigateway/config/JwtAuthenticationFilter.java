package com.insurance.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates JWT tokens at the gateway and injects user headers for downstream services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getURI().getPath();

        if (isPublicPath(requestPath)) {
            log.info("Request: {} User: {} Role: {}", requestPath, "anonymous", "PUBLIC");
            return chain.filter(exchange);
        }

        Optional<String> tokenOptional = extractToken(exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER));
        if (tokenOptional.isEmpty()) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = tokenOptional.get();
        String email;
        String role;
        try {
            if (!jwtUtil.validateToken(token)) {
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token");
            }

            email = jwtUtil.extractEmail(token);
            role = jwtUtil.extractRole(token);
            if (email == null || email.isBlank() || role == null || role.isBlank()) {
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT claims");
            }
        } catch (JwtException | IllegalArgumentException exception) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (isAdminPath(requestPath) && !"ADMIN".equalsIgnoreCase(role)) {
            return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, "Admin role is required");
        }

        log.info("Request: {} User: {} Role: {}", requestPath, email, role);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder
                        .header(USER_EMAIL_HEADER, email)
                        .header(USER_ROLE_HEADER, role))
                .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Optional<String> extractToken(String authorizationHeader) {
        return Optional.ofNullable(authorizationHeader)
                .filter(value -> value.startsWith(BEARER_PREFIX))
                .map(value -> value.substring(BEARER_PREFIX.length()))
                .filter(value -> !value.isBlank());
    }

    private boolean isPublicPath(String requestPath) {
        List<String> publicPaths = List.of(
                "/api/auth/register",
                "/api/auth/login"
        );

        return publicPaths.contains(requestPath)
                || requestPath.startsWith("/swagger-ui")
                || requestPath.startsWith("/v3/api-docs");
    }

    private boolean isAdminPath(String requestPath) {
        return requestPath.startsWith("/api/admin");
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        byte[] payload;
        try {
            String resolvedMessage = status == HttpStatus.UNAUTHORIZED
                    ? "Unauthorized"
                    : (message == null || message.isBlank() ? status.getReasonPhrase() : message);

            payload = objectMapper.writeValueAsBytes(new SimpleErrorResponse(status.value(), resolvedMessage));
        } catch (JsonProcessingException exception) {
            log.error("Failed to write error response", exception);
            payload = ("{\"status\":401,\"message\":\"Unauthorized\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer dataBuffer = response.bufferFactory().wrap(payload);
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * Minimal error payload for gateway authentication failures.
     */
    public record SimpleErrorResponse(int status, String message) {
    }
}
