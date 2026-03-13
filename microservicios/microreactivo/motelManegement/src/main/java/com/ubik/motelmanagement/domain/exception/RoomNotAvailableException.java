package com.ubik.motelmanagement.domain.exception;

public class RoomNotAvailableException extends BusinessRuleException {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}
