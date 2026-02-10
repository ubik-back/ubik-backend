package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/**
 * Controlador REST para operaciones autenticadas de Motel
 * 
 * ENDPOINTS PROTEGIDOS (requieren autenticación):
 * - GET /api/auth/motels/{userId} - Obtiene moteles del propietario autenticado
 * - GET /api/auth/motels/my-motels - Obtiene moteles del usuario autenticado
 * 
 * Este controlador maneja operaciones que requieren que el usuario esté autenticado
 * y que el ID del usuario en la URL coincida con el usuario autenticado.
 */
@RestController
@RequestMapping("/api/auth/motels")
public class AuthMotelController {

    private static final Logger log = LoggerFactory.getLogger(AuthMotelController.class);

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public AuthMotelController(MotelUseCasePort motelUseCasePort, MotelDtoMapper motelDtoMapper) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * PROTEGIDO - Obtiene todos los moteles del propietario autenticado
     * GET /api/auth/motels/{userId}
     * 
     * Este endpoint verifica que:
     * 1. El usuario esté autenticado (headers X-User-Id)
     * 2. El userId en la URL coincida con el usuario autenticado
     * 3. Retorna solo los moteles donde propertyId == userId
     * 
     * @param userId ID del propietario (debe coincidir con el usuario autenticado)
     * @param exchange ServerWebExchange para obtener headers de autenticación
     * @return Flux con los moteles del propietario
     */
    @GetMapping("/{userId}")
    public Flux<MotelResponse> getMotelsByAuthenticatedUser(
            @PathVariable Long userId,
            ServerWebExchange exchange) {
        
        log.info("=== GET /api/auth/motels/{} ===", userId);
        
        // 1. Obtener el ID del usuario autenticado desde los headers
        String authenticatedUserIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        
        log.debug("Headers recibidos:");
        exchange.getRequest().getHeaders().forEach((key, value) -> {
            log.debug("  {}: {}", key, value);
        });
        
        // 2. Validar que el usuario esté autenticado
        if (authenticatedUserIdHeader == null || authenticatedUserIdHeader.isBlank()) {
            log.error("Usuario no autenticado - Header X-User-Id ausente");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "Usuario no autenticado. Se requiere el header X-User-Id");
        }
        
        Long authenticatedUserId;
        try {
            authenticatedUserId = Long.parseLong(authenticatedUserIdHeader);
            log.info("Usuario autenticado ID: {}", authenticatedUserId);
        } catch (NumberFormatException e) {
            log.error("Header X-User-Id inválido: {}", authenticatedUserIdHeader);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "El header X-User-Id debe ser un número válido");
        }
        
        // 3. Validar que el userId de la URL coincida con el usuario autenticado
        if (!authenticatedUserId.equals(userId)) {
            log.warn("Acceso denegado: Usuario {} intentó acceder a moteles de usuario {}", 
                    authenticatedUserId, userId);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, 
                    "No tienes permiso para acceder a los moteles de otro usuario");
        }
        
        // 4. Retornar los moteles del propietario autenticado
        log.info("Buscando moteles con propertyId: {}", userId);
        
        return motelUseCasePort.getMotelsByPropertyId(userId)
                .doOnNext(motel -> log.debug("Motel encontrado: id={}, name={}, propertyId={}", 
                        motel.id(), motel.name(), motel.propertyId()))
                .doOnComplete(() -> log.info("Búsqueda completada para userId: {}", userId))
                .doOnError(error -> log.error("Error buscando moteles para userId {}: {}", 
                        userId, error.getMessage()))
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Obtiene los moteles del usuario autenticado sin pasar ID en URL
     * GET /api/auth/motels/my-motels
     * 
     * Alternativa más simple que obtiene automáticamente el userId del header
     * 
     * @param exchange ServerWebExchange para obtener headers de autenticación
     * @return Flux con los moteles del propietario autenticado
     */
    @GetMapping("/my-motels")
    public Flux<MotelResponse> getMyMotels(ServerWebExchange exchange) {
        
        log.info("=== GET /api/auth/motels/my-motels ===");
        
        // Obtener el ID del usuario autenticado desde los headers
        String authenticatedUserIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        
        log.debug("Headers recibidos:");
        exchange.getRequest().getHeaders().forEach((key, value) -> {
            log.debug("  {}: {}", key, value);
        });
        log.info("Header X-User-Id: {}", authenticatedUserIdHeader);
        
        // Validar que el usuario esté autenticado
        if (authenticatedUserIdHeader == null || authenticatedUserIdHeader.isBlank()) {
            log.error("Usuario no autenticado - Header X-User-Id ausente");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "Usuario no autenticado. Se requiere el header X-User-Id");
        }
        
        Long authenticatedUserId;
        try {
            authenticatedUserId = Long.parseLong(authenticatedUserIdHeader);
            log.info("Usuario autenticado ID parseado: {}", authenticatedUserId);
        } catch (NumberFormatException e) {
            log.error("Error parseando X-User-Id: {}", authenticatedUserIdHeader, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "El header X-User-Id debe ser un número válido");
        }
        
        // Retornar los moteles del propietario autenticado
        log.info("Buscando moteles con propertyId: {}", authenticatedUserId);
        
        return motelUseCasePort.getMotelsByPropertyId(authenticatedUserId)
                .doOnSubscribe(subscription -> log.debug("Iniciando búsqueda de moteles..."))
                .doOnNext(motel -> log.info("✓ Motel encontrado: id={}, name='{}', propertyId={}", 
                        motel.id(), motel.name(), motel.propertyId()))
                .doOnComplete(() -> log.info("✓ Búsqueda completada para propertyId: {}", authenticatedUserId))
                .doOnError(error -> log.error("✗ Error buscando moteles para propertyId {}: {}", 
                        authenticatedUserId, error.getMessage(), error))
                .map(motelDtoMapper::toResponse);
    }
}