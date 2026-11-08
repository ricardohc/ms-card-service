package com.banking.card.service.impl;

import com.banking.card.client.AccountValidationClient;
import com.banking.card.client.CustomerValidationClient;
import com.banking.card.document.CardDocument;
import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardUpdateRequest;
import com.banking.card.dto.ConsumptionRequest;
import com.banking.card.dto.CreditCardCreateRequest;
import com.banking.card.dto.DebitCardCreateRequest;
import com.banking.card.dto.EligibilityResponse;
import com.banking.card.dto.OperationAcceptedResponse;
import com.banking.card.event.CardEventProducer;
import com.banking.card.exception.AccountValidationException;
import com.banking.card.exception.InsufficientCreditException;
import com.banking.card.exception.InvalidCardTypeException;
import com.banking.card.mapper.CardMapper;
import com.banking.card.repository.CardAtomicOperations;
import com.banking.card.repository.CardRepository;
import com.banking.card.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

/** Unit tests of card business rules with StepVerifier (reactor-test). */
class CardServiceImplTest {

    private CardRepository cardRepository;
    private MovementRepository movementRepository;
    private CardAtomicOperations atomic;
    private CustomerValidationClient customerClient;
    private AccountValidationClient accountClient;
    private CardEventProducer eventProducer;
    private CardServiceImpl service;

    private final UUID customerId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);
        movementRepository = Mockito.mock(MovementRepository.class);
        atomic = Mockito.mock(CardAtomicOperations.class);
        customerClient = Mockito.mock(CustomerValidationClient.class);
        accountClient = Mockito.mock(AccountValidationClient.class);
        eventProducer = Mockito.mock(CardEventProducer.class);

        Mockito.when(eventProducer.cardIssued(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(eventProducer.cardUpdated(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(eventProducer.cardCancelled(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(eventProducer.movementRegistered(Mockito.any())).thenReturn(Mono.empty());

        service = new CardServiceImpl(cardRepository, movementRepository, atomic,
                new CardMapper(), customerClient, accountClient, eventProducer);
    }

    private CardDocument creditCard(BigDecimal available) {
        return CardDocument.builder()
                .id(cardId)
                .customerId(customerId)
                .type("CREDIT")
                .status("ACTIVE")
                .maskedPan("**** **** **** 1234")
                .creditLimit(new BigDecimal("5000.00"))
                .availableBalance(available)
                .currency("PEN")
                .build();
    }

    private CardDocument debitCard() {
        return CardDocument.builder()
                .id(cardId)
                .customerId(customerId)
                .type("DEBIT")
                .status("ACTIVE")
                .maskedPan("**** **** **** 4321")
                .linkedAccountId(accountId)
                .currency("PEN")
                .build();
    }

    private CreditCardCreateRequest creditRequest(String limit) {
        CreditCardCreateRequest credit = new CreditCardCreateRequest();
        credit.setCustomerId(customerId);
        credit.setType("CREDIT");
        credit.setCreditLimit(limit);
        return credit;
    }

    private CardCreateRequest wrap(Object payload) {
        return (CardCreateRequest) payload;
    }

    private ConsumptionRequest consumption(String amount) {
        ConsumptionRequest request = new ConsumptionRequest();
        request.setAmount(amount);
        request.setCurrency("PEN");
        request.setMerchant("Store X");
        return request;
    }

    @Test
    void createCreditCardWithPositiveLimitIsCreated() {
        Mockito.when(customerClient.customerExists(customerId)).thenReturn(Mono.just(true));
        Mockito.when(cardRepository.existsByCustomerIdAndTypeAndStatus(customerId, "CREDIT", "ACTIVE"))
                .thenReturn(Mono.just(false));
        Mockito.when(cardRepository.save(Mockito.any(CardDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.createCard(wrap(creditRequest("5000.00")), transactionId))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(customerId, response.getCustomerId());
                    org.junit.jupiter.api.Assertions.assertEquals("5000.00", response.getAvailableBalance());
                })
                .verifyComplete();
    }

    @Test
    void createDebitCardWithInactiveAccountFails() {
        DebitCardCreateRequest debit = new DebitCardCreateRequest();
        debit.setCustomerId(customerId);
        debit.setType("DEBIT");
        debit.setLinkedAccountId(accountId);

        Mockito.when(customerClient.customerExists(customerId)).thenReturn(Mono.just(true));
        Mockito.when(accountClient.accountActive(accountId)).thenReturn(Mono.just(false));

        StepVerifier.create(service.createCard(wrap(debit), transactionId))
                .expectError(AccountValidationException.class)
                .verify();
    }

    @Test
    void consumptionOnDebitCardIsRejected() {
        Mockito.when(cardRepository.findByIdAndStatusNot(cardId, "CANCELLED"))
                .thenReturn(Mono.just(debitCard()));

        StepVerifier.create(service.createConsumption(cardId, consumption("100.00"), transactionId))
                .expectError(InvalidCardTypeException.class)
                .verify();
    }

    @Test
    void consumptionAboveAvailableLineIsRejected() {
        Mockito.when(cardRepository.findByIdAndStatusNot(cardId, "CANCELLED"))
                .thenReturn(Mono.just(creditCard(new BigDecimal("100.00"))));

        StepVerifier.create(service.createConsumption(cardId, consumption("200.00"), transactionId))
                .expectError(InsufficientCreditException.class)
                .verify();
    }

    @Test
    void validConsumptionDecreasesLineAtomically() {
        Mockito.when(cardRepository.findByIdAndStatusNot(cardId, "CANCELLED"))
                .thenReturn(Mono.just(creditCard(new BigDecimal("500.00"))));
        Mockito.when(cardRepository.findById(cardId))
                .thenReturn(Mono.just(creditCard(new BigDecimal("500.00"))));
        Mockito.when(atomic.decrementAvailableBalance(cardId, new BigDecimal("100.00")))
                .thenReturn(Mono.just(true));
        Mockito.when(movementRepository.save(Mockito.any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.createConsumption(cardId, consumption("100.00"), transactionId))
                .assertNext((OperationAcceptedResponse response) -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ACCEPTED", response.getStatus());
                    org.junit.jupiter.api.Assertions.assertNotNull(response.getOperationId());
                })
                .verifyComplete();
    }

    @Test
    void patchBlockStatusIsAccepted() {
        CardDocument card = creditCard(new BigDecimal("500.00"));
        Mockito.when(cardRepository.findByIdAndStatusNot(cardId, "CANCELLED")).thenReturn(Mono.just(card));
        Mockito.when(cardRepository.save(Mockito.any(CardDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        CardUpdateRequest request = new CardUpdateRequest();
        request.setStatus(CardStatus.BLOCKED);

        StepVerifier.create(service.updateCard(cardId, request, transactionId))
                .assertNext(response ->
                        org.junit.jupiter.api.Assertions.assertEquals(CardStatus.BLOCKED, response.getStatus()))
                .verifyComplete();
    }

    @Test
    void eligibilityDetectsActiveCreditCard() {
        Mockito.when(cardRepository.existsByCustomerIdAndTypeAndStatus(customerId, "CREDIT", "ACTIVE"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.checkCardEligibility(customerId))
                .assertNext((EligibilityResponse response) ->
                        org.junit.jupiter.api.Assertions.assertTrue(response.getHasCreditCard()))
                .verifyComplete();
    }

    @Test
    void eligibilityWithoutCreditCardReturnsFalse() {
        Mockito.when(cardRepository.existsByCustomerIdAndTypeAndStatus(customerId, "CREDIT", "ACTIVE"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(service.checkCardEligibility(customerId))
                .assertNext((EligibilityResponse response) ->
                        org.junit.jupiter.api.Assertions.assertFalse(response.getHasCreditCard()))
                .verifyComplete();
    }

    @Test
    void deleteMarksCardCancelled() {
        CardDocument card = creditCard(new BigDecimal("500.00"));
        Mockito.when(cardRepository.findByIdAndStatusNot(cardId, "CANCELLED")).thenReturn(Mono.just(card));
        Mockito.when(cardRepository.save(Mockito.any(CardDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.deactivateCard(cardId, transactionId))
                .verifyComplete();
        org.junit.jupiter.api.Assertions.assertEquals("CANCELLED", card.getStatus());
    }
}
