package com.example.paymentservice.application.result;

import com.acme.payments.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentIntentView(
        UUID id,
        String externalReference,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        String mpPreferenceId,
        Long mpPaymentId,
        String redirectUrl,
        String lastProviderStatus,
        Instant createdAt,
        Instant updatedAt
) {}