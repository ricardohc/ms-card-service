package com.banking.card.service;

import com.banking.card.dto.BalanceResponse;
import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardPage;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardUpdateRequest;
import com.banking.card.dto.ConsumptionRequest;
import com.banking.card.dto.EligibilityResponse;
import com.banking.card.dto.MovementListResponse;
import com.banking.card.dto.OperationAcceptedResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Business API for card management (WebFlux/Reactor only). */
public interface CardService {

    Mono<CardPage> findAllCards(int page, int size);

    Mono<CardResponse> createCard(CardCreateRequest request, UUID transactionId);

    Mono<CardResponse> findCardById(UUID cardId);

    Mono<CardResponse> updateCard(UUID cardId, CardUpdateRequest request, UUID transactionId);

    Mono<Void> deactivateCard(UUID cardId, UUID transactionId);

    Mono<OperationAcceptedResponse> createConsumption(UUID cardId, ConsumptionRequest request, UUID transactionId);

    Mono<BalanceResponse> findCardBalance(UUID cardId);

    Mono<MovementListResponse> findLastMovements(UUID cardId);

    Mono<EligibilityResponse> checkCardEligibility(UUID customerId);
}
