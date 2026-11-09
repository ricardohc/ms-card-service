package com.banking.card.exception;

public class InsufficientCreditException extends DomainException {

    public InsufficientCreditException(String message) {
        super(422, "INSUFFICIENT_CREDIT", message);
    }
}
