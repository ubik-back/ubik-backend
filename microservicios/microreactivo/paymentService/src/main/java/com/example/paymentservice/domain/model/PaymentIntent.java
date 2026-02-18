package com.example.paymentservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PaymentIntent {

    private final UUID id;
    private final String externalReference;
    private final Money money;
    private PaymentStatus status;
    private final PaymentProvider provider;
    private String mpPreferenceId;
    private Long mpPaymentId;
    private String redirectUrl;
    private String lastProviderStatus;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    // Constructor for creating new intent
    public PaymentIntent(UUID id, String externalReference, Money money, PaymentProvider provider) {
        this.id = id;
        this.externalReference = externalReference;
        this.money = money;
        this.provider = provider;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }

    // Full constructor for reconstitution from DB
    public PaymentIntent(UUID id, String externalReference, Money money,
                         PaymentStatus status, PaymentProvider provider,
                         String mpPreferenceId, Long mpPaymentId, String redirectUrl,
                         String lastProviderStatus, Instant createdAt, Instant updatedAt, Long version) {
        this.id = id;
        this.externalReference = externalReference;
        this.money = money;
        this.status = status;
        this.provider = provider;
        this.mpPreferenceId = mpPreferenceId;
        this.mpPaymentId = mpPaymentId;
        this.redirectUrl = redirectUrl;
        this.lastProviderStatus = lastProviderStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public void applyPreference(String preferenceId, String initPoint) {
        this.mpPreferenceId = preferenceId;
        this.redirectUrl = initPoint;
        this.updatedAt = Instant.now();
    }

    public void applyPaymentUpdate(Long paymentId, PaymentStatus newStatus, String providerStatus) {
        this.mpPaymentId = paymentId;
        this.status = newStatus;
        this.lastProviderStatus = providerStatus;
        this.updatedAt = Instant.now();
        this.version = this.version + 1;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
        this.version = this.version + 1;
    }

    // Getters
    public UUID getId() { return id; }
    public String getExternalReference() { return externalReference; }
    public Money getMoney() { return money; }
    public PaymentStatus getStatus() { return status; }
    public PaymentProvider getProvider() { return provider; }
    public String getMpPreferenceId() { return mpPreferenceId; }
    public Long getMpPaymentId() { return mpPaymentId; }
    public String getRedirectUrl() { return redirectUrl; }
    public String getLastProviderStatus() { return lastProviderStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}