package com.example.gateway.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración CORS para Spring Cloud Gateway
 * 
 * IMPORTANTE: Esta configuración permite peticiones desde Angular con Authorization header
 * Maneja correctamente las preflight requests (OPTIONS) requeridas cuando se envía JWT
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Permitir origen específico de Angular (NO usar "*" con credentials)
        corsConfig.setAllowedOrigins(List.of("http://localhost:4200"));
        
        // Permitir TODOS los métodos HTTP (especialmente OPTIONS para preflight)
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "PATCH", 
            "OPTIONS",
            "HEAD"
        ));
        
        // CRÍTICO: Permitir Authorization header (requerido para JWT)
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "X-User-Username",
            "X-User-Role"
        ));
        
        // Exponer headers que Angular necesita leer
        corsConfig.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-User-Username",
            "X-User-Role"
        ));
        
        // Permitir credenciales (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);
        
        // Cache de preflight request (evita múltiples OPTIONS)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Aplicar a TODAS las rutas
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
