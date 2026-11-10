package com.banking.card.event;

import com.banking.card.document.CardDocument;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventProducer {

    public static final String TOPIC = "banking.card.events.v1";

    private final ReactiveKafkaProducerTemplate<String, CardEvent> kafkaTemplate;

    public Mono<Void> cardIssued(CardDocument card, String correlationId) {
        return send(CardEvent.INSERT, card, correlationId);
    }

    public Mono<Void> cardUpdated(CardDocument card, String correlationId) {
        return send(CardEvent.UPDATE, card, correlationId);
    }

    public Mono<Void> cardCancelled(CardDocument card, String correlationId) {
        return send(CardEvent.DELETE, card, correlationId);
    }

    private Mono<Void> send(String eventType, CardDocument card, String correlationId) {
        CardEvent event = buildEvent(eventType, card, correlationId);
        return kafkaTemplate.send(TOPIC, card.getId().toString(), event)
                .doOnSuccess(result -> log.info("Event {} sent for card {}", eventType, card.getId()))
                .onErrorResume(ex -> {
                    log.error("Failed to send event {} for card {}: {}", eventType, card.getId(), ex.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    private CardEvent buildEvent(String eventType, CardDocument card, String correlationId) {
        return CardEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .correlationId(correlationId)
                .payload(CardEvent.CardPayload.builder()
                        .id(card.getId())
                        .customerId(card.getCustomerId())
                        .type(card.getType())
                        .status(card.getStatus())
                        .maskedPan(card.getMaskedPan())
                        .linkedAccountId(card.getLinkedAccountId())
                        .creditLimit(card.getCreditLimit() == null ? null : card.getCreditLimit().toPlainString())
                        .availableBalance(card.getAvailableBalance() == null ? null : card.getAvailableBalance().toPlainString())
                        .expirationDate(card.getExpirationDate() == null ? null : card.getExpirationDate().toString())
                        .build())
                .build();
    }
}