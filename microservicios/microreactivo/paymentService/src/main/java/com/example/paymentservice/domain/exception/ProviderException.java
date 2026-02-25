package com.example.paymentservice.domain.exception;

public class ProviderException extends DomainException {
    public ProviderException(String message) { super(message); }
    public ProviderException(String message, Throwable cause) { super(message, cause); }
}