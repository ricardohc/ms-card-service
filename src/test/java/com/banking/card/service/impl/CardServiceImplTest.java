package com.banking.card.service.impl;

import com.banking.card.document.AccountReferenceDocument;
import com.banking.card.document.CardDocument;
import com.banking.card.document.CustomerReferenceDocument;
import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardUpdateRequest;
import com.banking.card.dto.CreditCardCreateRequest;
import com.banking.card.dto.DebitCardCreateRequest;
import com.banking.card.event.CardEventProducer;
import com.banking.card.exception.AccountValidationException;
import com.banking.card.mapper.CardMapper;
import com.banking.card.repository.AccountReferenceRepository;
import com.banking.card.repository.CardRepository;
import com.banking.card.repository.CustomerReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

class CardServiceImplTest {

    private CardRepository cardRepository;
    private CustomerReferenceRepository customerReferenceRepository;
    private AccountReferenceRepository accountReferenceRepository;
    private CardEventProducer eventProducer;
    private CardServiceImpl service;

    private final UUID customerId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);
        customerReferenceRepository = Mockito.mock(CustomerReferenceRepository.class);
        accountReferenceRepository = Mockito.mock(AccountReferenceRepository.class);
        eventProducer = Mockito.mock(CardEventProducer.class);

        Mockito.when(eventProducer.cardIssued(Mockito.any(), Mockito.anyString())).thenReturn(Mono.empty());
        Mockito.when(eventProducer.cardUpdated(Mockito.any(), Mockito.anyString())).thenReturn(Mono.empty());
        Mockito.when(eventProducer.cardCancelled(Mockito.any(), Mockito.anyString())).thenReturn(Mono.empty());

        service = new CardServiceImpl(cardRepository, new CardMapper(),
                customerReferenceRepository, accountReferenceRepository, eventProducer);
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

    @Test
    void createCreditCardWithPositiveLimitIsCreated() {
        Mockito.when(customerReferenceRepository.findById(customerId.toString()))
                .thenReturn(Mono.just(CustomerReferenceDocument.builder().customerId(customerId.toString()).build()));
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

        Mockito.when(customerReferenceRepository.findById(customerId.toString()))
                .thenReturn(Mono.just(CustomerReferenceDocument.builder().customerId(customerId.toString()).build()));
        Mockito.when(accountReferenceRepository.findById(accountId.toString()))
                .thenReturn(Mono.just(AccountReferenceDocument.builder().id(accountId.toString()).status("BLOCKED").build()));

        StepVerifier.create(service.createCard(wrap(debit), transactionId))
                .expectError(AccountValidationException.class)
                .verify();
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