package com.insurance.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Logs incoming requests and responses passing through the API Gateway.
 * Helps with traceability and debugging across services.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod() == null
                ? "UNKNOWN"
                : exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String traceId = resolveTraceId();
        log.info("Incoming request: {} {} traceId={}", method, path, traceId);

        return chain.filter(exchange)
                .doOnSuccess(done -> log.info("Response completed: {} {} traceId={}", method, path, traceId));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        return traceId == null || traceId.isBlank() ? "N/A" : traceId;
    }
}
