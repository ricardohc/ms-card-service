package com.banking.card.exception;

public class AccountValidationException extends DomainException {

    public AccountValidationException(String message) {
        super(422, "ACCOUNT_VALIDATION_FAILED", message);
    }
}
