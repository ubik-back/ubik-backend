package com.example.paymentservice.adapters.in.web.dto;

import java.time.Instant;

public record ApiErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String traceId
) {
    public static ApiErrorResponse of(String code, String message, String traceId) {
        return new ApiErrorResponse(code, message, Instant.now(), traceId);
    }
}