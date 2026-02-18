package com.example.paymentservice.application.result;


import java.util.UUID;

public record CreateCheckoutProResult(
        UUID paymentIntentId,
        String externalReference,
        String initPoint,
        String status
) {}