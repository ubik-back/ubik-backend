package com.example.paymentservice.domain.exception;

import com.example.paymentservice.adapters.in.web.dto.ApiErrorResponse;
import com.example.paymentservice.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleNotFound(NotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("NOT_FOUND", ex.getMessage(), newTraceId())));
    }

    @ExceptionHandler(InvalidSignatureException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleInvalidSignature(InvalidSignatureException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of("INVALID_SIGNATURE", ex.getMessage(), newTraceId())));
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleIdempotencyConflict(IdempotencyConflictException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("IDEMPOTENCY_CONFLICT", ex.getMessage(), newTraceId())));
    }

    @ExceptionHandler(ProviderException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleProvider(ProviderException ex) {
        log.error("Provider error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.of("PROVIDER_ERROR", ex.getMessage(), newTraceId())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleValidation(WebExchangeBindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("VALIDATION_ERROR", msg, newTraceId())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", newTraceId())));
    }

    private String newTraceId() {
        return UUID.randomUUID().toString();
    }
}
