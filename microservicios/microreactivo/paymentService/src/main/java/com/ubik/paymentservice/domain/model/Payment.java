package com.ubik.paymentservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        Long id,
        Long reservationId,
        Long userId,
        Long motelId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String mercadopagoPaymentId,
        String mercadopagoPreferenceId,
        String mercadopagoPublicKey,
        String initPoint,
        String failureReason,
        BigDecimal marketplaceFee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Payment withStatus(PaymentStatus newStatus) {
        return new Payment(id, reservationId, userId, motelId, amount, currency,
                newStatus, mercadopagoPaymentId, mercadopagoPreferenceId, mercadopagoPublicKey,
                initPoint, failureReason, marketplaceFee, createdAt, LocalDateTime.now());
    }

    public Payment withPreference(String preferenceId, String initPoint, String publicKey, BigDecimal fee) {
        return new Payment(id, reservationId, userId, motelId, amount, currency,
                status, null, preferenceId, publicKey, initPoint, failureReason,
                fee, createdAt, LocalDateTime.now());
    }

    public Payment withMpPaymentId(String mpPaymentId) {
        return new Payment(id, reservationId, userId, motelId, amount, currency,
                status, mpPaymentId, mercadopagoPreferenceId, mercadopagoPublicKey,
                initPoint, failureReason, marketplaceFee, createdAt, LocalDateTime.now());
    }

    public Payment withFailure(String reason) {
        return new Payment(id, reservationId, userId, motelId, amount, currency,
                PaymentStatus.REJECTED, mercadopagoPaymentId, mercadopagoPreferenceId, mercadopagoPublicKey,
                initPoint, reason, marketplaceFee, createdAt, LocalDateTime.now());
    }
}
