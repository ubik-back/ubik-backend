package com.example.gateway.application.filter;

import com.example.gateway.domain.port.out.JwtValidatorPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtValidatorPort jwtValidatorPort;

    public JwtAuthenticationFilter(JwtValidatorPort jwtValidatorPort) {
        this.jwtValidatorPort = jwtValidatorPort;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        // 1. Rutas públicas (no requieren JWT)
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 2. Extraer JWT del Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            // 3. Validar token y extraer claims
            Map<String, Object> claims = jwtValidatorPort.validateToken(token);

            String username = claims.get("sub").toString();
            String role     = claims.get("role").toString();

            // 4. Mutar request y agregar headers para microservicios
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Username", username)
                    .header("X-User-Role", role) // <-- CORREGIDO
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            // 5. Registrar Authentication (solo útil en el Gateway)
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
    // Rutas de autenticación
    if (path.startsWith("/api/auth/")) {
        return true;
    }
    
    // Actuator endpoints
    if (path.startsWith("/actuator/")) {
        return true;
    }
    
    // Moteles (solo lectura, sin autenticación)
    if (path.equals("/api/motels") || path.startsWith("/api/motels/")) {
        // Solo permitir GET sin autenticación
        return true;
    }
    
    return false;
}

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}




