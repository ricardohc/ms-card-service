package com.banking.card.exception;

public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String message, Throwable cause) {
        super(502, "EXTERNAL_SERVICE_ERROR", message, cause);
    }
}