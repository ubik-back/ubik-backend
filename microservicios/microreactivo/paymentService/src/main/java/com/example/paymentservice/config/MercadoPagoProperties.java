package com.example.paymentservice.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String webhookSecret,
        String notificationUrl,
        BackUrls backUrls,
        long webhookSkewMillis,
        int connectionTimeoutMs,
        int socketTimeoutMs
) {
    public record BackUrls(String success, String pending, String failure) {}

    public MercadoPagoProperties {
        if (webhookSkewMillis <= 0) webhookSkewMillis = 300_000L; // 5 min default
        if (connectionTimeoutMs <= 0) connectionTimeoutMs = 2000;
        if (socketTimeoutMs <= 0) socketTimeoutMs = 5000;
    }
}
