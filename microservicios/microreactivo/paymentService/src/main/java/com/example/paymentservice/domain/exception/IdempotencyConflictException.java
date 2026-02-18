package com.example.paymentservice.domain.exception;

public class IdempotencyConflictException extends DomainException {
    private final String resourceId;
    public IdempotencyConflictException(String resourceId) {
        super("Idempotency key already used for resource: " + resourceId);
        this.resourceId = resourceId;
    }
    public String getResourceId() { return resourceId; }
}
