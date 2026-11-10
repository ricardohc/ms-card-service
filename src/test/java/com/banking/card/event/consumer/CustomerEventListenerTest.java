package com.banking.card.event.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banking.card.document.CustomerReferenceDocument;
import com.banking.card.repository.CustomerReferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

class CustomerEventListenerTest {

    private CustomerReferenceRepository repository;
    private CustomerEventListener listener;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CustomerReferenceRepository.class);
        listener = new CustomerEventListener(repository, new ObjectMapper());
    }

    @Test
    void insertEventCreatesNewCustomerReference() {
        String message = """
                {
                  "eventType": "i",
                  "payload": {
                    "customerId": "CUST-1",
                    "customerType": "PERSONAL",
                    "documentType": "DNI",
                    "documentNumber": "12345678",
                    "firstName": "Juan",
                    "lastName": "Perez",
                    "email": "juan@mail.com",
                    "phoneNumber": "999888777",
                    "profile": "STANDARD",
                    "status": "ACTIVE"
                  }
                }
                """;
        when(repository.save(any(CustomerReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        listener.listenCustomerEvents(message);

        ArgumentCaptor<CustomerReferenceDocument> captor =
                ArgumentCaptor.forClass(CustomerReferenceDocument.class);
        verify(repository).save(captor.capture());
        CustomerReferenceDocument doc = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("CUST-1", doc.getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals("PERSONAL", doc.getCustomerType());
        org.junit.jupiter.api.Assertions.assertEquals("DNI", doc.getDocumentType());
        org.junit.jupiter.api.Assertions.assertEquals("12345678", doc.getDocumentNumber());
        org.junit.jupiter.api.Assertions.assertEquals("Juan", doc.getFirstName());
        org.junit.jupiter.api.Assertions.assertEquals("Perez", doc.getLastName());
        org.junit.jupiter.api.Assertions.assertEquals("juan@mail.com", doc.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", doc.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(doc.getCreatedAt());
        org.junit.jupiter.api.Assertions.assertNotNull(doc.getUpdatedAt());
    }

    @Test
    void insertEventIgnoresInvalidPayload() {
        String message = "{\"eventType\":\"i\",\"payload\":{}}";

        listener.listenCustomerEvents(message);

        verify(repository, never()).save(any());
    }

    @Test
    void insertEventIgnoresMalformedJson() {
        listener.listenCustomerEvents("not-a-json");

        verify(repository, never()).save(any());
    }

    @Test
    void updateEventModifiesExistingCustomer() {
        CustomerReferenceDocument existing = CustomerReferenceDocument.builder()
                .customerId("CUST-1")
                .firstName("Old")
                .email("old@mail.com")
                .status("ACTIVE")
                .build();
        when(repository.findById("CUST-1")).thenReturn(Mono.just(existing));
        when(repository.save(any(CustomerReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        String message = """
                {
                  "eventType": "u",
                  "payload": {
                    "customerId": "CUST-1",
                    "firstName": "New",
                    "email": "new@mail.com",
                    "status": "BLOCKED"
                  }
                }
                """;

        listener.listenCustomerEvents(message);

        ArgumentCaptor<CustomerReferenceDocument> captor =
                ArgumentCaptor.forClass(CustomerReferenceDocument.class);
        verify(repository).save(captor.capture());
        CustomerReferenceDocument saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("New", saved.getFirstName());
        org.junit.jupiter.api.Assertions.assertEquals("new@mail.com", saved.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", saved.getStatus());
    }

    @Test
    void updateEventCreatesMissingCustomer() {
        when(repository.findById("CUST-NEW")).thenReturn(Mono.empty());
        when(repository.save(any(CustomerReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        String message = """
                {
                  "eventType": "u",
                  "payload": {
                    "customerId": "CUST-NEW",
                    "firstName": "Ana",
                    "status": "ACTIVE"
                  }
                }
                """;

        listener.listenCustomerEvents(message);

        ArgumentCaptor<CustomerReferenceDocument> captor =
                ArgumentCaptor.forClass(CustomerReferenceDocument.class);
        verify(repository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("CUST-NEW", captor.getValue().getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals("Ana", captor.getValue().getFirstName());
    }

    @Test
    void deleteEventMarksCustomerInactive() {
        CustomerReferenceDocument existing = CustomerReferenceDocument.builder()
                .customerId("CUST-1")
                .status("ACTIVE")
                .build();
        when(repository.findById("CUST-1")).thenReturn(Mono.just(existing));
        when(repository.save(any(CustomerReferenceDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        String message = "{\"eventType\":\"d\",\"payload\":{\"customerId\":\"CUST-1\"}}";

        listener.listenCustomerEvents(message);

        ArgumentCaptor<CustomerReferenceDocument> captor =
                ArgumentCaptor.forClass(CustomerReferenceDocument.class);
        verify(repository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("INACTIVE", captor.getValue().getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(captor.getValue().getUpdatedAt());
    }

    @Test
    void unknownEventTypeIsIgnored() {
        String message = "{\"eventType\":\"x\",\"payload\":{\"customerId\":\"CUST-1\"}}";

        listener.listenCustomerEvents(message);

        verify(repository, never()).save(any());
    }

    @Test
    void updateToleratesSaveError() {
        CustomerReferenceDocument existing = CustomerReferenceDocument.builder()
                .customerId("CUST-1")
                .status("ACTIVE")
                .build();
        when(repository.findById("CUST-1")).thenReturn(Mono.just(existing));
        when(repository.save(any(CustomerReferenceDocument.class)))
                .thenReturn(Mono.error(new RuntimeException("db down")));

        String message = "{\"eventType\":\"u\",\"payload\":{\"customerId\":\"CUST-1\"}}";

        listener.listenCustomerEvents(message);

        verify(repository).save(any());
    }
}