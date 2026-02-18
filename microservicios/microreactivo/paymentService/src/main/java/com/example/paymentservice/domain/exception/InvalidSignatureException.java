package com.example.paymentservice.domain.exception;
public class InvalidSignatureException extends DomainException {
    public InvalidSignatureException(String message) { super(message); }
}