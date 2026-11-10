package com.banking.card.exception;

public class DuplicateCardException extends DomainException {

    public DuplicateCardException(String message) {
        super(409, "DUPLICATE_CARD", message);
    }
}