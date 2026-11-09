package com.banking.card.exception;

public class InvalidCardTypeException extends DomainException {

    public InvalidCardTypeException(String message) {
        super(422, "INVALID_CARD_OPERATION", message);
    }
}
