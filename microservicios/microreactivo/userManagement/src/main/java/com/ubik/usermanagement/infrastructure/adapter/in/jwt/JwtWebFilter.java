package com.ubik.usermanagement.infrastructure.adapter.in.jwt;

import com.ubik.usermanagement.infrastructure.adapter.out.jwt.JwtAdapter;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.List;
import java.util.Map;

@Component
public class JwtWebFilter {

    private final JwtAdapter jwtAdapter;

    public JwtWebFilter(JwtAdapter jwtAdapter) {
        this.jwtAdapter = jwtAdapter;
    }

    public WebFilter authenticationFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();

            if (path.startsWith("/api/auth/")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String token = authHeader.substring(7);

            try {
                Map<String, Object> claims = jwtAdapter.extractClaims(token);
                String username = (String) claims.get("sub");
                String role = (String) claims.get("role");

                if (username != null && jwtAdapter.isTokenValid(token, username)) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                }
            } catch (Exception e) {
                return chain.filter(exchange);
            }

            return chain.filter(exchange);
        };
    }
}


