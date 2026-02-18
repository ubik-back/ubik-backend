package com.example.paymentservice.application.result;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundResult(
        UUID paymentIntentId,
        Long refundId,
        BigDecimal refundedAmount,
        String status
) {}
