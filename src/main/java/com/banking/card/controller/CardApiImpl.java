package com.banking.card.controller;

import com.banking.card.api.CardApi;
import com.banking.card.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class CardApiImpl implements CardApi {

    @Override
    public Mono<ResponseEntity<CardPage>> findAllCards(
            String xCorrelationID, String xChannelID, Integer page, Integer size,
            ServerWebExchange exchange) {
        CardPage p = new CardPage();
        p.setContent(List.of());
        p.setPage(page);
        p.setSize(size);
        p.setTotalElements(0);
        return Mono.just(ResponseEntity.ok(p));
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> createCard(
            String xCorrelationID, String xChannelID, UUID xTransactionID,
            Mono<CardCreateRequest> cardCreateRequest, ServerWebExchange exchange) {
        return cardCreateRequest.map(req -> {
            CardResponse r = new CardResponse();
            r.setId(UUID.randomUUID());
            r.setStatus(CardStatus.ACTIVE);
            r.setMaskedPan("**** **** **** 1234");
            r.setExpirationDate(LocalDate.now().plusYears(5));
            /*
            if (req.getActualInstance() instanceof CreditCardCreateRequest credit) {
                r.setCustomerId(credit.getCustomerId());
                r.setType(CardType.CREDIT);
                r.setCreditLimit(credit.getCreditLimit());
                r.setAvailableBalance(credit.getCreditLimit());
            } else if (req.getActualInstance() instanceof DebitCardCreateRequest debit) {
                r.setCustomerId(debit.getCustomerId());
                r.setType(CardType.DEBIT);
                r.setLinkedAccountId(debit.getLinkedAccountId());
            }*/
            return ResponseEntity.status(HttpStatus.CREATED).body(r);
        });
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> findCardById(
            UUID cardId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        CardResponse r = new CardResponse();
        r.setId(cardId);
        r.setStatus(CardStatus.ACTIVE);
        r.setMaskedPan("**** **** **** 1234");
        r.setExpirationDate(LocalDate.now().plusYears(5));
        return Mono.just(ResponseEntity.ok(r));
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> updateCard(
            UUID cardId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, Mono<CardUpdateRequest> cardUpdateRequest,
            ServerWebExchange exchange) {
        return cardUpdateRequest.map(req -> {
            CardResponse r = new CardResponse();
            r.setId(cardId);
            r.setStatus(req.getStatus());
            r.setMaskedPan("**** **** **** 1234");
            r.setExpirationDate(LocalDate.now().plusYears(5));
            return ResponseEntity.ok(r);
        });
    }

    @Override
    public Mono<ResponseEntity<Void>> deactivateCard(
            UUID cardId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<OperationAcceptedResponse>> createConsumption(
            UUID cardId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, Mono<ConsumptionRequest> consumptionRequest,
            ServerWebExchange exchange) {
        return consumptionRequest.map(req -> {
            OperationAcceptedResponse r = new OperationAcceptedResponse();
            r.setOperationId(UUID.randomUUID());
            r.setStatus("ACCEPTED");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(r);
        });
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> findCardBalance(
            UUID cardId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        BalanceResponse r = new BalanceResponse();
        r.setCardId(cardId);
        r.setAvailableBalance("5000.00");
        r.setCurrency("PEN");
        return Mono.just(ResponseEntity.ok(r));
    }

    @Override
    public Mono<ResponseEntity<MovementListResponse>> findLastMovements(
            UUID cardId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        MovementListResponse r = new MovementListResponse();
        r.setMovements(List.of());
        return Mono.just(ResponseEntity.ok(r));
    }

    @Override
    public Mono<ResponseEntity<EligibilityResponse>> checkCardEligibility(
            UUID customerId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        EligibilityResponse r = new EligibilityResponse();
        r.setCustomerId(customerId);
        r.setHasCreditCard(true);
        return Mono.just(ResponseEntity.ok(r));
    }
}
