package com.banking.card.controller;

import com.banking.card.api.CardApi;
import com.banking.card.dto.BalanceResponse;
import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardPage;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardUpdateRequest;
import com.banking.card.dto.ConsumptionRequest;
import com.banking.card.dto.EligibilityResponse;
import com.banking.card.dto.MovementListResponse;
import com.banking.card.dto.OperationAcceptedResponse;
import com.banking.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Thin adapter: generated CardApi -> CardService. No business logic here. */
@RestController
@RequiredArgsConstructor
public class CardApiImpl implements CardApi {

    private final CardService cardService;

    @Override
    public Mono<ResponseEntity<CardPage>> findAllCards(
            String xCorrelationID, String xChannelID, Integer page, Integer size,
            ServerWebExchange exchange) {
        return cardService.findAllCards(page, size)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> createCard(
            String xCorrelationID, String xChannelID, UUID xTransactionID,
            Mono<CardCreateRequest> cardCreateRequest, ServerWebExchange exchange) {
        return cardCreateRequest
                .flatMap(request -> cardService.createCard(request, xTransactionID))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> findCardById(
            UUID cardId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        return cardService.findCardById(cardId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> updateCard(
            UUID cardId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, Mono<CardUpdateRequest> cardUpdateRequest,
            ServerWebExchange exchange) {
        return cardUpdateRequest
                .flatMap(request -> cardService.updateCard(cardId, request, xTransactionID))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deactivateCard(
            UUID cardId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, ServerWebExchange exchange) {
        return cardService.deactivateCard(cardId, xTransactionID)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    @Override
    public Mono<ResponseEntity<OperationAcceptedResponse>> createConsumption(
            UUID cardId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, Mono<ConsumptionRequest> consumptionRequest,
            ServerWebExchange exchange) {
        return consumptionRequest
                .flatMap(request -> cardService.createConsumption(cardId, request, xTransactionID))
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> findCardBalance(
            UUID cardId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        return cardService.findCardBalance(cardId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<MovementListResponse>> findLastMovements(
            UUID cardId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        return cardService.findLastMovements(cardId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<EligibilityResponse>> checkCardEligibility(
            UUID customerId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        return cardService.checkCardEligibility(customerId)
                .map(ResponseEntity::ok);
    }
}
