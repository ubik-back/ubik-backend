package com.example.paymentservice.adapters.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentDtos {

    public record CreateCheckoutProRequest(
            @NotBlank String externalReference,
            @NotBlank String title,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String currency
    ) {}

    public record CreateCheckoutProResponse(
            String paymentIntentId,
            String externalReference,
            String initPoint,
            String status
    ) {}

    public record PaymentIntentResponse(
            String id,
            String externalReference,
            BigDecimal amount,
            String currency,
            String status,
            String provider,
            String mpPreferenceId,
            Long mpPaymentId,
            String redirectUrl,
            String lastProviderStatus,
            String createdAt,
            String updatedAt
    ) {}

    public record RefundRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount
    ) {}

    public record RefundResponse(
            String paymentIntentId,
            Long refundId,
            BigDecimal refundedAmount,
            String status
    ) {}
}