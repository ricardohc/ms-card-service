package com.banking.card.event;

import com.banking.card.document.CardDocument;
import com.banking.card.document.MovementDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Produces domain events on topic banking.card.events.v1.
 * Events: CardIssued, CardUpdated, CardCancelled, CardMovementRegistered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventProducer {

    public static final String TOPIC = "banking.card.events.v1";

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public Mono<Void> cardIssued(CardDocument card) {
        return send("CardIssued", card.getId(), baseEvent(card, "CardIssued"));
    }

    public Mono<Void> cardUpdated(CardDocument card) {
        return send("CardUpdated", card.getId(), baseEvent(card, "CardUpdated"));
    }

    public Mono<Void> cardCancelled(CardDocument card) {
        return send("CardCancelled", card.getId(), baseEvent(card, "CardCancelled"));
    }

    public Mono<Void> movementRegistered(MovementDocument movement) {
        Map<String, Object> event = Map.of(
                "eventType", "CardMovementRegistered",
                "cardId", String.valueOf(movement.getCardId()),
                "movementId", String.valueOf(movement.getId()),
                "type", movement.getType(),
                "amount", movement.getAmount().toPlainString(),
                "timestamp", movement.getTimestamp().toString());
        return send("CardMovementRegistered", movement.getCardId(), event);
    }

    private Map<String, Object> baseEvent(CardDocument card, String eventType) {
        return Map.of(
                "eventType", eventType,
                "cardId", String.valueOf(card.getId()),
                "customerId", String.valueOf(card.getCustomerId()),
                "type", card.getType(),
                "status", card.getStatus());
    }

    private Mono<Void> send(String eventType, UUID key, Object payload) {
        return kafkaTemplate.send(TOPIC, String.valueOf(key), payload)
                .doOnSuccess(result -> log.info("Event {} sent for key {}", eventType, key))
                .onErrorResume(ex -> {
                    // Event publishing must not break the main flow; log and continue.
                    log.error("Failed to send event {} for key {}: {}", eventType, key, ex.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
