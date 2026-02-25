package com.example.paymentservice.domain.model;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    AUTHORIZED,
    IN_PROCESS,
    IN_MEDIATION,
    REJECTED,
    CANCELLED,
    REFUNDED,
    CHARGED_BACK,
    EXPIRED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED
                || this == REFUNDED || this == CHARGED_BACK || this == EXPIRED;
    }
}