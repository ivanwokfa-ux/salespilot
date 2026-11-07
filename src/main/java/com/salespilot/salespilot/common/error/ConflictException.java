package com.salespilot.salespilot.common.error;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}