package com.example.paymentservice.application.command;

import java.math.BigDecimal;

public record CreateCheckoutProCommand(
        String externalReference,
        String title,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String userId
) {}