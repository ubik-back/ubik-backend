package com.example.gateway.application.config;

import com.example.gateway.application.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationFilter jwtFilter) {
        
        return http
                // Deshabilitar CSRF para APIs REST
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // Deshabilitar autenticación básica y formularios
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                // Configurar reglas de autorización
                .authorizeExchange(exchanges -> exchanges
                        // Rutas de autenticación - públicas
                        .pathMatchers("/api/auth/**").permitAll()
                        
                        // Actuator - público (o restringir según necesites)
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Moteles - lectura pública (GET), escritura requiere auth
                        .pathMatchers("/api/motels/**").permitAll()
                        .pathMatchers("/api/rooms/**").permitAll()
                        .pathMatchers("/api/services/**").permitAll()
                        
                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )
                
                // IMPORTANTE: Activar el filtro JWT
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
}