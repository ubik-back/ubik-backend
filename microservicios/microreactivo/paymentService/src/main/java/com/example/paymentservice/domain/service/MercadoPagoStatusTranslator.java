package com.example.paymentservice.domain.service;

import com.acme.payments.domain.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoStatusTranslator {

    /**
     * Translates MP payment status + status_detail to domain PaymentStatus.
     * Reference: https://www.mercadopago.com.ar/developers/en/docs/checkout-api/response-handling/collection-results
     */
    public PaymentStatus translate(String mpStatus, String mpStatusDetail) {
        if (mpStatus == null) return PaymentStatus.PENDING;

        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "authorized" -> PaymentStatus.AUTHORIZED;
            case "in_process", "pending" -> {
                if ("pending_waiting_payment".equals(mpStatusDetail)) yield PaymentStatus.PENDING;
                yield PaymentStatus.IN_PROCESS;
            }
            case "in_mediation" -> PaymentStatus.IN_MEDIATION;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> {
                if ("expired".equals(mpStatusDetail)) yield PaymentStatus.EXPIRED;
                yield PaymentStatus.CANCELLED;
            }
            case "refunded" -> PaymentStatus.REFUNDED;
            case "charged_back" -> PaymentStatus.CHARGED_BACK;
            default -> PaymentStatus.PENDING;
        };
    }
}