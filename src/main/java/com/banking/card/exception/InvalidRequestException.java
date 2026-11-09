package com.banking.card.exception;

public class InvalidRequestException extends DomainException {

    public InvalidRequestException(String message) {
        super(400, "INVALID_REQUEST", message);
    }
}
