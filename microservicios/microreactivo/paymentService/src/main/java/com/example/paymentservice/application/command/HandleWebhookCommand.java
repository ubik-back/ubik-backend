package com.example.paymentservice.application.command;

public record HandleWebhookCommand(
        String type,
        String dataId,
        String xRequestId,
        String xSignature,
        String rawBody
) {}