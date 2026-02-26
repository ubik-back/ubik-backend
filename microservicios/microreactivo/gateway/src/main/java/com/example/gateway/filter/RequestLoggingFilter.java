package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for logging all requests passing through the gateway.
 * Useful for auditing and debugging.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String userId = getHeader(request, "X-User-Id");
        String userRole = getHeader(request, "X-User-Role");
        String path = request.getPath().toString();
        String method = request.getMethod().toString();
        String remoteAddress = request.getRemoteAddress() != null ?
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        logger.info("Gateway Request: {} {} | User: {} | Role: {} | IP: {}",
            method, path, userId != null ? userId : "anonymous", userRole != null ? userRole : "none", remoteAddress);

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                exchange.getResponse().getStatusCode().value() : 0;

            logger.info("Gateway Response: {} {} | Status: {} | Duration: {}ms | User: {}",
                method, path, statusCode, duration, userId != null ? userId : "anonymous");
        }));
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        var headers = request.getHeaders().get(headerName);
        return (headers != null && !headers.isEmpty()) ? headers.get(0) : null;
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
