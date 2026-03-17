package com.ubik.motelmanagement.infrastructure.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Filtro para capturar la hora del cliente enviada en la cabecera X-Client-Time.
 * Permite sincronizar la lógica de negocio con la zona horaria del usuario (ej. Colombia).
 */
@Component
public class ClientTimeFilter implements WebFilter {

    public static final String CLIENT_TIME_CONTEXT_KEY = "X-Client-Time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientTime = exchange.getRequest().getHeaders().getFirst("X-Client-Time");
        
        if (clientTime == null || clientTime.isBlank()) {
            clientTime = exchange.getRequest().getQueryParams().getFirst("client_time");
        }

        if (clientTime != null && !clientTime.isBlank()) {
            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put(CLIENT_TIME_CONTEXT_KEY, clientTime));
        }
        
        return chain.filter(exchange);
    }
}
