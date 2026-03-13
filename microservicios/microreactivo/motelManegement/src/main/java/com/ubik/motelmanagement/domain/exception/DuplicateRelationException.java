package com.ubik.motelmanagement.domain.exception;

public class DuplicateRelationException extends BusinessRuleException {
    public DuplicateRelationException(String message) {
        super(message);
    }
}
