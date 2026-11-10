package com.banking.card.exception;

public class InvalidCardStateException extends DomainException {

    public InvalidCardStateException(String message) {
        super(422, "INVALID_CARD_STATE", message);
    }
}