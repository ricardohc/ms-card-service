package com.banking.card.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banking.card.document.CardDocument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

class CardEventProducerTest {

    private ReactiveKafkaProducerTemplate<String, CardEvent> kafkaTemplate;
    private CardEventProducer producer;

    private final UUID cardId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        SenderResult result = Mockito.mock(SenderResult.class);
        when(kafkaTemplate.send(anyString(), anyString(), any(CardEvent.class)))
                .thenReturn(Mono.just(result));
        producer = new CardEventProducer(kafkaTemplate);
    }

    private CardDocument creditCard() {
        return CardDocument.builder()
                .id(cardId)
                .customerId(customerId)
                .type("CREDIT")
                .status("ACTIVE")
                .maskedPan("**** **** **** 1234")
                .linkedAccountId(accountId)
                .creditLimit(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("4500.00"))
                .currency("PEN")
                .expirationDate(LocalDate.of(2028, 1, 1))
                .build();
    }

    @Test
    void cardIssuedSendsInsertEvent() {
        StepVerifier.create(producer.cardIssued(creditCard(), "corr-1"))
                .verifyComplete();

        ArgumentCaptor<CardEvent> captor = ArgumentCaptor.forClass(CardEvent.class);
        verify(kafkaTemplate, times(1))
                .send(eq(CardEventProducer.TOPIC), eq(cardId.toString()), captor.capture());

        CardEvent event = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(CardEvent.INSERT, event.getEventType());
        org.junit.jupiter.api.Assertions.assertEquals("corr-1", event.getCorrelationId());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getEventId());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getOccurredAt());
        org.junit.jupiter.api.Assertions.assertEquals(cardId, event.getPayload().getId());
        org.junit.jupiter.api.Assertions.assertEquals(customerId, event.getPayload().getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals("CREDIT", event.getPayload().getType());
        org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", event.getPayload().getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("**** **** **** 1234", event.getPayload().getMaskedPan());
        org.junit.jupiter.api.Assertions.assertEquals(accountId, event.getPayload().getLinkedAccountId());
        org.junit.jupiter.api.Assertions.assertEquals("5000.00", event.getPayload().getCreditLimit());
        org.junit.jupiter.api.Assertions.assertEquals("4500.00", event.getPayload().getAvailableBalance());
        org.junit.jupiter.api.Assertions.assertEquals("2028-01-01", event.getPayload().getExpirationDate());
    }

    @Test
    void cardUpdatedSendsUpdateEvent() {
        StepVerifier.create(producer.cardUpdated(creditCard(), "corr-2"))
                .verifyComplete();

        ArgumentCaptor<CardEvent> captor = ArgumentCaptor.forClass(CardEvent.class);
        verify(kafkaTemplate).send(eq(CardEventProducer.TOPIC), eq(cardId.toString()), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(CardEvent.UPDATE, captor.getValue().getEventType());
        org.junit.jupiter.api.Assertions.assertEquals("corr-2", captor.getValue().getCorrelationId());
    }

    @Test
    void cardCancelledSendsDeleteEvent() {
        StepVerifier.create(producer.cardCancelled(creditCard(), "corr-3"))
                .verifyComplete();

        ArgumentCaptor<CardEvent> captor = ArgumentCaptor.forClass(CardEvent.class);
        verify(kafkaTemplate).send(eq(CardEventProducer.TOPIC), eq(cardId.toString()), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(CardEvent.DELETE, captor.getValue().getEventType());
        org.junit.jupiter.api.Assertions.assertEquals("corr-3", captor.getValue().getCorrelationId());
    }

    @Test
    void sendToleratesKafkaFailure() {
        when(kafkaTemplate.send(anyString(), anyString(), any(CardEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("kafka down")));

        StepVerifier.create(producer.cardIssued(creditCard(), "corr"))
                .verifyComplete();

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(CardEvent.class));
    }

    @Test
    void sendHandlesNullOptionalFields() {
        CardDocument debit = CardDocument.builder()
                .id(cardId)
                .customerId(customerId)
                .type("DEBIT")
                .status("ACTIVE")
                .maskedPan("**** **** **** 9999")
                .linkedAccountId(accountId)
                .creditLimit(null)
                .availableBalance(null)
                .currency("PEN")
                .expirationDate(null)
                .build();

        StepVerifier.create(producer.cardIssued(debit, "corr"))
                .verifyComplete();

        ArgumentCaptor<CardEvent> captor = ArgumentCaptor.forClass(CardEvent.class);
        verify(kafkaTemplate).send(anyString(), anyString(), captor.capture());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getPayload().getCreditLimit());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getPayload().getAvailableBalance());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getPayload().getExpirationDate());
    }

    @Test
    void eachEventGetsUniqueEventId() {
        StepVerifier.create(producer.cardIssued(creditCard(), "c")).verifyComplete();
        StepVerifier.create(producer.cardIssued(creditCard(), "c")).verifyComplete();

        ArgumentCaptor<CardEvent> captor = ArgumentCaptor.forClass(CardEvent.class);
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), captor.capture());
        var events = captor.getAllValues();
        org.junit.jupiter.api.Assertions.assertNotEquals(events.get(0).getEventId(), events.get(1).getEventId());
    }
}