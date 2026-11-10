package com.banking.card.service;

import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardPage;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardUpdateRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CardService {

    Mono<CardPage> findAllCards(int page, int size);

    Mono<CardResponse> createCard(CardCreateRequest request, UUID transactionId);

    Mono<CardResponse> findCardById(UUID cardId);

    Mono<CardResponse> updateCard(UUID cardId, CardUpdateRequest request, UUID transactionId);

    Mono<Void> deactivateCard(UUID cardId, UUID transactionId);
}