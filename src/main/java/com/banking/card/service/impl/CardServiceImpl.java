package com.banking.card.service.impl;

import com.banking.card.document.CardDocument;
import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardPage;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardType;
import com.banking.card.dto.CardUpdateRequest;
import com.banking.card.dto.CreditCardCreateRequest;
import com.banking.card.dto.DebitCardCreateRequest;
import com.banking.card.event.CardEventProducer;
import com.banking.card.exception.AccountValidationException;
import com.banking.card.exception.CardNotFoundException;
import com.banking.card.exception.DuplicateCardException;
import com.banking.card.exception.InvalidCardStateException;
import com.banking.card.exception.InvalidCardTypeException;
import com.banking.card.mapper.CardMapper;
import com.banking.card.repository.AccountReferenceRepository;
import com.banking.card.repository.CardRepository;
import com.banking.card.repository.CustomerReferenceRepository;
import com.banking.card.service.CardService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final String STATUS_ACTIVE = CardStatus.ACTIVE.getValue();
    private static final String STATUS_CANCELLED = CardStatus.CANCELLED.getValue();
    private static final String TYPE_CREDIT = CardType.CREDIT.getValue();
    private static final String TYPE_DEBIT = CardType.DEBIT.getValue();
    private static final String DEFAULT_CURRENCY = "PEN";
    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";

    private final CardRepository cardRepository;
    private final CardMapper mapper;
    private final CustomerReferenceRepository customerReferenceRepository;
    private final AccountReferenceRepository accountReferenceRepository;
    private final CardEventProducer eventProducer;

    @Override
    public Mono<CardPage> findAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Mono<List<CardResponse>> content = cardRepository.findAllBy(pageable)
                .map(mapper::toResponse)
                .collectList();
        Mono<Long> total = cardRepository.countByStatusNot(STATUS_CANCELLED);
        return Mono.zip(content, total)
                .map(tuple -> {
                    CardPage result = new CardPage();
                    result.setContent(tuple.getT1());
                    result.setPage(page);
                    result.setSize(size);
                    result.setTotalElements(tuple.getT2().intValue());
                    return result;
                });
    }

    @Override
    public Mono<CardResponse> createCard(CardCreateRequest request, UUID transactionId) {
        return Mono.defer(() -> {
            if (request instanceof CreditCardCreateRequest credit) {
                return createCreditCard(credit, transactionId);
            } else if (request instanceof DebitCardCreateRequest debit) {
                return createDebitCard(debit, transactionId);
            }
            return Mono.error(new InvalidCardTypeException("Card payload does not match oneOf CREDIT/DEBIT"));
        });
    }

    private Mono<CardResponse> createCreditCard(CreditCardCreateRequest credit, UUID transactionId) {
        BigDecimal limit = new BigDecimal(credit.getCreditLimit());
        if (limit.signum() <= 0) {
            return Mono.error(new InvalidCardTypeException("creditLimit must be greater than 0 for CREDIT cards"));
        }
        return customerReferenceRepository.findById(credit.getCustomerId().toString())
                .switchIfEmpty(Mono.error(new AccountValidationException("Customer does not exist in local reference")))
                .then(duplicateCardCheck(credit.getCustomerId(), TYPE_CREDIT))
                .then(Mono.defer(() -> {
                    CardDocument card = CardDocument.builder()
                            .id(UUID.randomUUID())
                            .customerId(credit.getCustomerId())
                            .type(TYPE_CREDIT)
                            .status(STATUS_ACTIVE)
                            .maskedPan(generateMaskedPan())
                            .creditLimit(limit)
                            .availableBalance(limit)
                            .currency(DEFAULT_CURRENCY)
                            .expirationDate(LocalDate.now(java.time.ZoneOffset.UTC).plusYears(4))
                            .build();
                    return cardRepository.save(card)
                            .flatMap(saved -> eventProducer.cardIssued(saved, transactionId.toString())
                                    .thenReturn(saved))
                            .map(mapper::toResponse);
                }));
    }

    private Mono<CardResponse> createDebitCard(DebitCardCreateRequest debit, UUID transactionId) {
        return customerReferenceRepository.findById(debit.getCustomerId().toString())
                .switchIfEmpty(Mono.error(new AccountValidationException("Customer does not exist in local reference")))
                .then(accountReferenceRepository.findById(debit.getLinkedAccountId().toString()))
                .switchIfEmpty(Mono.error(new AccountValidationException("linkedAccountId does not exist in local reference")))
                .filter(account -> ACCOUNT_STATUS_ACTIVE.equals(account.getStatus()))
                .switchIfEmpty(Mono.error(new AccountValidationException("linkedAccountId is not ACTIVE")))
                .then(Mono.defer(() -> {
                    CardDocument card = CardDocument.builder()
                            .id(UUID.randomUUID())
                            .customerId(debit.getCustomerId())
                            .type(TYPE_DEBIT)
                            .status(STATUS_ACTIVE)
                            .maskedPan(generateMaskedPan())
                            .linkedAccountId(debit.getLinkedAccountId())
                            .currency(DEFAULT_CURRENCY)
                            .expirationDate(LocalDate.now(java.time.ZoneOffset.UTC).plusYears(4))
                            .build();
                    return cardRepository.save(card)
                            .flatMap(saved -> eventProducer.cardIssued(saved, transactionId.toString())
                                    .thenReturn(saved))
                            .map(mapper::toResponse);
                }));
    }

    private Mono<Void> duplicateCardCheck(UUID customerId, String type) {
        return cardRepository.existsByCustomerIdAndTypeAndStatus(customerId, type, STATUS_ACTIVE)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateCardException("Customer already has an ACTIVE credit card"))
                        : Mono.<Void>empty());
    }

    private String generateMaskedPan() {
        String last4 = String.format("%04d", (int) (Math.random() * 10000));
        return "**** **** **** " + last4;
    }

    @Override
    public Mono<CardResponse> findCardById(UUID cardId) {
        return getActiveCard(cardId).map(mapper::toResponse);
    }

    @Override
    public Mono<CardResponse> updateCard(UUID cardId, CardUpdateRequest request, UUID transactionId) {
        return getActiveCard(cardId)
                .flatMap(card -> {
                    if (request.getStatus() != null) {
                        String target = request.getStatus().getValue();
                        if (STATUS_CANCELLED.equals(target)) {
                            return Mono.error(new InvalidCardStateException("Use DELETE for definitive cancellation"));
                        }
                        card.setStatus(target);
                    }
                    return cardRepository.save(card)
                            .flatMap(saved -> eventProducer.cardUpdated(saved, transactionId.toString())
                                    .thenReturn(saved))
                            .map(mapper::toResponse);
                });
    }

    @Override
    public Mono<Void> deactivateCard(UUID cardId, UUID transactionId) {
        return getActiveCard(cardId)
                .flatMap(card -> {
                    card.setStatus(STATUS_CANCELLED);
                    return cardRepository.save(card)
                            .flatMap(saved -> eventProducer.cardCancelled(saved, transactionId.toString()));
                });
    }

    private Mono<CardDocument> getActiveCard(UUID cardId) {
        return cardRepository.findByIdAndStatusNot(cardId, STATUS_CANCELLED)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found: " + cardId)));
    }
}