package com.example.paymentservice.config;



import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String internalApiKey,
        RateLimit rateLimit
) {
    public record RateLimit(int writesPerMinute, int webhookPerMinute) {
        public RateLimit {
            if (writesPerMinute <= 0) writesPerMinute = 30;
            if (webhookPerMinute <= 0) webhookPerMinute = 120;
        }
    }
}