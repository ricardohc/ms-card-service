package com.banking.card.exception;

public class CardNotFoundException extends DomainException {

    public CardNotFoundException(String message) {
        super(404, "CARD_NOT_FOUND", message);
    }
}