package com.example.gateway.application.config;

import com.example.gateway.application.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad para Spring Cloud Gateway
 * 
 * CRÍTICO: Permite OPTIONS sin autenticación para soportar CORS preflight
 */
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
                        // ========================================
                        // CRÍTICO: Permitir OPTIONS sin autenticación
                        // (requerido para CORS preflight)
                        // ========================================
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // ========================================
                        // RUTAS COMPLETAMENTE PÚBLICAS (sin token)
                        // ========================================
                        
                        // Autenticación - público
                        .pathMatchers("/api/auth/**").permitAll()
                        
                        // Actuator - público
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Moteles - SOLO lectura es pública
                        .pathMatchers(HttpMethod.GET, "/api/motels/**").permitAll()
                        
                        // Habitaciones - SOLO lectura es pública
                        .pathMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        
                        // Servicios - SOLO lectura es pública
                        .pathMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                        
                        // ========================================
                        // RUTAS QUE REQUIEREN AUTENTICACIÓN
                        // ========================================
                        
                        // Cualquier POST, PUT, DELETE en motels/rooms/services
                        .pathMatchers(HttpMethod.POST, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/motels/**").authenticated()
                        
                        .pathMatchers(HttpMethod.POST, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/rooms/**").authenticated()
                        
                        .pathMatchers(HttpMethod.POST, "/api/services/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/services/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/services/**").authenticated()
                        
                        // Reservas - siempre requieren autenticación
                        .pathMatchers("/api/reservations/**").authenticated()
                        
                        // Perfil de usuario - requiere autenticación
                        .pathMatchers("/api/user/**").authenticated()
                        
                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )
                
                // Activar el filtro JWT DESPUÉS de CORS
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
}
