package com.banking.card.event.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banking.card.document.AccountReferenceDocument;
import com.banking.card.repository.AccountReferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

class AccountEventListenerTest {

    private AccountReferenceRepository repository;
    private AccountEventListener listener;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AccountReferenceRepository.class);
        listener = new AccountEventListener(repository, new ObjectMapper());
    }

    @Test
    void insertEventCreatesNewAccountReference() {
        String message = """
                {
                  "eventType": "i",
                  "payload": {
                    "id": "ACC-1",
                    "accountNumber": "001-123",
                    "customerId": "CUST-1",
                    "accountType": "SAVINGS",
                    "profile": "STANDARD",
                    "status": "ACTIVE",
                    "balance": "1500.00",
                    "currency": "PEN",
                    "createdAt": "2024-01-15T10:00:00Z"
                  }
                }
                """;
        when(repository.save(any(AccountReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        listener.listenAccountEvents(message);

        ArgumentCaptor<AccountReferenceDocument> captor =
                ArgumentCaptor.forClass(AccountReferenceDocument.class);
        verify(repository).save(captor.capture());
        AccountReferenceDocument doc = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("ACC-1", doc.getId());
        org.junit.jupiter.api.Assertions.assertEquals("001-123", doc.getAccountNumber());
        org.junit.jupiter.api.Assertions.assertEquals("CUST-1", doc.getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals("SAVINGS", doc.getAccountType());
        org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", doc.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("1500.00"), doc.getBalance());
        org.junit.jupiter.api.Assertions.assertEquals("PEN", doc.getCurrency());
        org.junit.jupiter.api.Assertions.assertNotNull(doc.getCreatedAt());
        org.junit.jupiter.api.Assertions.assertNotNull(doc.getUpdatedAt());
    }

    @Test
    void insertEventIgnoresInvalidPayload() {
        String message = "{\"eventType\":\"i\",\"payload\":{}}";

        listener.listenAccountEvents(message);

        verify(repository, never()).save(any());
    }

    @Test
    void insertEventIgnoresMalformedJson() {
        listener.listenAccountEvents("not-a-json");

        verify(repository, never()).save(any());
    }

    @Test
    void updateEventModifiesExistingAccount() {
        AccountReferenceDocument existing = AccountReferenceDocument.builder()
                .id("ACC-1")
                .accountNumber("001-123")
                .customerId("CUST-1")
                .status("ACTIVE")
                .balance(new BigDecimal("1000.00"))
                .currency("PEN")
                .build();
        when(repository.findById("ACC-1")).thenReturn(Mono.just(existing));
        when(repository.save(any(AccountReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        String message = """
                {
                  "eventType": "u",
                  "payload": {
                    "id": "ACC-1",
                    "accountNumber": "001-999",
                    "status": "BLOCKED",
                    "balance": "250.00"
                  }
                }
                """;

        listener.listenAccountEvents(message);

        ArgumentCaptor<AccountReferenceDocument> captor =
                ArgumentCaptor.forClass(AccountReferenceDocument.class);
        verify(repository).save(captor.capture());
        AccountReferenceDocument saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("001-999", saved.getAccountNumber());
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", saved.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("250.00"), saved.getBalance());
    }

    @Test
    void updateEventCreatesMissingAccount() {
        when(repository.findById("ACC-NEW")).thenReturn(Mono.empty());
        when(repository.save(any(AccountReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        String message = """
                {
                  "eventType": "u",
                  "payload": {
                    "id": "ACC-NEW",
                    "customerId": "CUST-9",
                    "balance": "777.77"
                  }
                }
                """;

        listener.listenAccountEvents(message);

        ArgumentCaptor<AccountReferenceDocument> captor =
                ArgumentCaptor.forClass(AccountReferenceDocument.class);
        verify(repository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("ACC-NEW", captor.getValue().getId());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("777.77"), captor.getValue().getBalance());
    }

    @Test
    void deleteEventMarksAccountInactive() {
        AccountReferenceDocument existing = AccountReferenceDocument.builder()
                .id("ACC-1")
                .status("ACTIVE")
                .build();
        when(repository.findById("ACC-1")).thenReturn(Mono.just(existing));
        when(repository.save(any(AccountReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        String message = "{\"eventType\":\"d\",\"payload\":{\"id\":\"ACC-1\"}}";

        listener.listenAccountEvents(message);

        ArgumentCaptor<AccountReferenceDocument> captor =
                ArgumentCaptor.forClass(AccountReferenceDocument.class);
        verify(repository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("INACTIVE", captor.getValue().getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(captor.getValue().getUpdatedAt());
    }

    @Test
    void unknownEventTypeIsIgnored() {
        String message = "{\"eventType\":\"x\",\"payload\":{\"id\":\"ACC-1\"}}";

        listener.listenAccountEvents(message);

        verify(repository, never()).save(any());
    }

    @Test
    void updateToleratesSaveError() {
        AccountReferenceDocument existing = AccountReferenceDocument.builder()
                .id("ACC-1")
                .status("ACTIVE")
                .build();
        when(repository.findById("ACC-1")).thenReturn(Mono.just(existing));
        when(repository.save(any(AccountReferenceDocument.class)))
                .thenReturn(Mono.error(new RuntimeException("db down")));

        String message = "{\"eventType\":\"u\",\"payload\":{\"id\":\"ACC-1\"}}";

        listener.listenAccountEvents(message);

        verify(repository).save(any());
    }
}