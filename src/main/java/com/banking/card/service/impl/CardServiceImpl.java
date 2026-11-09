package com.banking.card.service.impl;

import com.banking.card.client.AccountValidationClient;
import com.banking.card.client.CustomerValidationClient;
import com.banking.card.document.CardDocument;
import com.banking.card.document.MovementDocument;
import com.banking.card.dto.BalanceResponse;
import com.banking.card.dto.CardCreateRequest;
import com.banking.card.dto.CardPage;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardType;
import com.banking.card.dto.CardUpdateRequest;
import com.banking.card.dto.ConsumptionRequest;
import com.banking.card.dto.CreditCardCreateRequest;
import com.banking.card.dto.DebitCardCreateRequest;
import com.banking.card.dto.EligibilityResponse;
import com.banking.card.dto.MovementListResponse;
import com.banking.card.dto.OperationAcceptedResponse;
import com.banking.card.event.CardEventProducer;
import com.banking.card.exception.AccountValidationException;
import com.banking.card.exception.CardNotFoundException;
import com.banking.card.exception.DuplicateCardException;
import com.banking.card.exception.InsufficientCreditException;
import com.banking.card.exception.InvalidCardStateException;
import com.banking.card.exception.InvalidCardTypeException;
import com.banking.card.exception.InvalidRequestException; // <-- IMPORTANTE
import com.banking.card.mapper.CardMapper;
import com.banking.card.repository.CardAtomicOperations;
import com.banking.card.repository.CardRepository;
import com.banking.card.repository.MovementRepository;
import com.banking.card.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Reactor-based implementation of card business rules.
 * All cross-service validations go through typed clients (HTTP APIs only);
 * this service never reads another service's database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final String STATUS_ACTIVE = CardStatus.ACTIVE.getValue();
    private static final String STATUS_CANCELLED = CardStatus.CANCELLED.getValue();
    private static final String TYPE_CREDIT = CardType.CREDIT.getValue();
    private static final String TYPE_DEBIT = CardType.DEBIT.getValue();
    private static final String DEFAULT_CURRENCY = "PEN";

    private final CardRepository cardRepository;
    private final MovementRepository movementRepository;
    private final CardAtomicOperations atomic;
    private final CardMapper mapper;
    private final CustomerValidationClient customerClient;
    private final AccountValidationClient accountClient;
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
                return createCreditCard(credit);
            } else if (request instanceof DebitCardCreateRequest debit) {
                return createDebitCard(debit);
            }
            return Mono.error(new InvalidCardTypeException("Card payload does not match oneOf CREDIT/DEBIT"));
        });
    }

    private Mono<CardResponse> createCreditCard(CreditCardCreateRequest credit) {
        BigDecimal limit = new BigDecimal(credit.getCreditLimit());
        if (limit.signum() <= 0) {
            return Mono.error(new InvalidCardTypeException(
                    "creditLimit must be greater than 0 for CREDIT cards"));
        }
        return customerClient.customerExists(credit.getCustomerId())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new AccountValidationException("Customer does not exist")))
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
                            .flatMap(saved -> eventProducer.cardIssued(saved).thenReturn(saved))
                            .map(mapper::toResponse);
                }));
    }

    private Mono<CardResponse> createDebitCard(DebitCardCreateRequest debit) {
        return customerClient.customerExists(debit.getCustomerId())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new AccountValidationException("Customer does not exist")))
                .then(accountClient.accountActive(debit.getLinkedAccountId()))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new AccountValidationException(
                        "linkedAccountId does not exist or is not ACTIVE")))
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
                            .flatMap(saved -> eventProducer.cardIssued(saved).thenReturn(saved))
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
                            return Mono.error(new InvalidCardStateException(
                                    "Use DELETE for definitive cancellation"));
                        }
                        card.setStatus(target);
                    }
                    return cardRepository.save(card)
                            .flatMap(saved -> eventProducer.cardUpdated(saved).thenReturn(saved))
                            .map(mapper::toResponse);
                });
    }

    @Override
    public Mono<Void> deactivateCard(UUID cardId, UUID transactionId) {
        return getActiveCard(cardId)
                .flatMap(card -> {
                    card.setStatus(STATUS_CANCELLED);
                    return cardRepository.save(card)
                            .flatMap(saved -> eventProducer.cardCancelled(saved).then());
                });
    }

    @Override
    public Mono<OperationAcceptedResponse> createConsumption(UUID cardId, ConsumptionRequest request, UUID transactionId) {
        return Mono.defer(() -> {
            BigDecimal amount = new BigDecimal(request.getAmount());

            // 1. VALIDACIÓN TEMPRANA: El monto debe ser mayor a cero (Evita que llegue a Mongo o Kafka)
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Mono.error(new InvalidRequestException(
                        "The transaction amount must be greater than zero"));
            }

            return getActiveCard(cardId)
                    .flatMap(card -> {
                        // 2. Validar TIPO: Si es débito, rechazar rápido con 422
                        if (!TYPE_CREDIT.equals(card.getType())) {
                            return Mono.error(new InvalidCardTypeException(
                                    "Consumptions apply only to CREDIT cards"));
                        }
                        // 3. Validar ESTADO: Si no está activa, rechazar con 422
                        if (!STATUS_ACTIVE.equals(card.getStatus())) {
                            return Mono.error(new InvalidCardStateException("Card is not ACTIVE"));
                        }
                        // 4. Validar SALDO: Si el monto es mayor, rechazar con 422
                        if (card.getAvailableBalance() == null || amount.compareTo(card.getAvailableBalance()) > 0) {
                            return Mono.error(new InsufficientCreditException(
                                    "Amount exceeds available credit line"));
                        }
                        // 5. Procesar atómicamente y registrar movimiento
                        return atomic.decrementAvailableBalance(cardId, amount)
                                .flatMap(updated -> updated
                                        ? recordMovement(cardId, "CONSUMPTION", amount, request)
                                        : Mono.error(new InsufficientCreditException(
                                        "Amount exceeds available credit line")));
                    })
                    .map(movementId -> {
                        OperationAcceptedResponse response = new OperationAcceptedResponse();
                        response.setOperationId(movementId);
                        response.setStatus("ACCEPTED");
                        return response;
                    });
        });
    }

    private Mono<UUID> recordMovement(UUID cardId, String type, BigDecimal amount, ConsumptionRequest request) {
        MovementDocument movement = MovementDocument.builder()
                .id(UUID.randomUUID())
                .cardId(cardId)
                .type(type)
                .amount(amount)
                .currency(request.getCurrency())
                .merchant(request.getMerchant())
                .timestamp(Instant.now())
                .build();
        return movementRepository.save(movement)
                .map(saved -> {
                    eventProducer.movementRegistered(saved).subscribe();
                    return saved.getId();
                });
    }

    @Override
    public Mono<BalanceResponse> findCardBalance(UUID cardId) {
        return getActiveCard(cardId)
                .map(card -> {
                    BalanceResponse response = new BalanceResponse();
                    response.setCardId(card.getId());
                    if (TYPE_CREDIT.equals(card.getType())) {
                        response.setAvailableBalance(card.getAvailableBalance().toPlainString());
                    } else {
                        response.setAvailableBalance(BigDecimal.ZERO.toPlainString());
                    }
                    response.setCurrency(card.getCurrency());
                    return response;
                });
    }

    @Override
    public Mono<MovementListResponse> findLastMovements(UUID cardId) {
        return getActiveCard(cardId)
                .thenMany(movementRepository.findTop10ByCardIdOrderByTimestampDesc(cardId))
                .collectList()
                .map(mapper::toMovementList);
    }

    @Override
    public Mono<EligibilityResponse> checkCardEligibility(UUID customerId) {
        return cardRepository.existsByCustomerIdAndTypeAndStatus(customerId, TYPE_CREDIT, STATUS_ACTIVE)
                .map(hasCard -> {
                    EligibilityResponse response = new EligibilityResponse();
                    response.setCustomerId(customerId);
                    response.setHasCreditCard(hasCard);
                    return response;
                });
    }

    private Mono<CardDocument> getActiveCard(UUID cardId) {
        return cardRepository.findByIdAndStatusNot(cardId, STATUS_CANCELLED)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found: " + cardId)));
    }
}