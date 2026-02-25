package com.example.paymentservice.application.command;


import java.math.BigDecimal;
import java.util.UUID;

public record RefundPaymentCommand(
        UUID paymentIntentId,
        BigDecimal amount,
        String idempotencyKey
) {}
