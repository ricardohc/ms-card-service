package com.banking.card.event.consumer;

import com.banking.card.document.CustomerReferenceDocument;
import com.banking.card.repository.CustomerReferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final CustomerReferenceRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "banking.customer.events.v1", groupId = "card-service-group")
    public void listenCustomerEvents(@Payload String message) {
        log.info("Message received from banking.customer.events.v1: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();
            JsonNode payload = root.path("payload");

            if (payload.isMissingNode() || payload.path("customerId").isMissingNode()) {
                log.warn("Evento inválido recibido (sin payload o customerId): {}", message);
                return;
            }

            String customerId = payload.path("customerId").asText();
            log.info("Processing eventType={} for customerId={}", eventType, customerId);

            switch (eventType) {
                case "i" -> insertCustomer(customerId, payload);
                case "u" -> updateCustomer(customerId, payload);
                case "d" -> deleteCustomer(customerId);
                default -> log.debug("Evento no manejado: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error procesando evento de cliente: {}", message, e);
        }
    }

    private void insertCustomer(String customerId, JsonNode payload) {
        CustomerReferenceDocument doc = CustomerReferenceDocument.builder()
                .customerId(customerId)
                .customerType(payload.path("customerType").asText(null))
                .documentType(payload.path("documentType").asText(null))
                .documentNumber(payload.path("documentNumber").asText(null))
                .firstName(payload.path("firstName").asText(null))
                .lastName(payload.path("lastName").asText(null))
                .email(payload.path("email").asText(null))
                .phoneNumber(payload.path("phoneNumber").asText(null))
                .profile(payload.path("profile").asText(null))
                .status(payload.path("status").asText("ACTIVE"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        log.info("Saving new customer reference for {}", customerId);
        repository.save(doc).subscribe(
                saved -> log.info("Customer reference insertado para {}", customerId),
                error -> log.error("Error insertando customer reference {}", customerId, error)
        );
    }

    private void updateCustomer(String customerId, JsonNode payload) {
        repository.findById(customerId)
                .flatMap(doc -> {
                    applyPayload(doc, payload);
                    doc.setUpdatedAt(Instant.now());
                    log.info("Updating customer reference for {}", customerId);
                    return repository.save(doc);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CustomerReferenceDocument doc = CustomerReferenceDocument.builder()
                            .customerId(customerId)
                            .customerType(payload.path("customerType").asText(null))
                            .documentType(payload.path("documentType").asText(null))
                            .documentNumber(payload.path("documentNumber").asText(null))
                            .firstName(payload.path("firstName").asText(null))
                            .lastName(payload.path("lastName").asText(null))
                            .email(payload.path("email").asText(null))
                            .phoneNumber(payload.path("phoneNumber").asText(null))
                            .profile(payload.path("profile").asText(null))
                            .status(payload.path("status").asText(null))
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    log.info("Creating missing customer reference during update for {}", customerId);
                    return repository.save(doc);
                }))
                .subscribe(
                        saved -> log.info("Customer reference actualizado para {}", customerId),
                        error -> log.error("Error actualizando customer reference {}", customerId, error)
                );
    }

    private void deleteCustomer(String customerId) {
        repository.findById(customerId)
                .flatMap(doc -> {
                    doc.setStatus("INACTIVE");
                    doc.setUpdatedAt(Instant.now());
                    log.info("Deactivating customer reference for {}", customerId);
                    return repository.save(doc);
                })
                .subscribe(
                        saved -> log.info("Customer reference desactivado para {}", customerId),
                        error -> log.error("Error desactivando customer reference {}", customerId, error)
                );
    }

    private void applyPayload(CustomerReferenceDocument existing, JsonNode payload) {
        String customerType = payload.path("customerType").asText(null);
        String documentType = payload.path("documentType").asText(null);
        String documentNumber = payload.path("documentNumber").asText(null);
        String firstName = payload.path("firstName").asText(null);
        String lastName = payload.path("lastName").asText(null);
        String email = payload.path("email").asText(null);
        String phoneNumber = payload.path("phoneNumber").asText(null);
        String profile = payload.path("profile").asText(null);
        String status = payload.path("status").asText(null);

        if (customerType != null) existing.setCustomerType(customerType);
        if (documentType != null) existing.setDocumentType(documentType);
        if (documentNumber != null) existing.setDocumentNumber(documentNumber);
        if (firstName != null) existing.setFirstName(firstName);
        if (lastName != null) existing.setLastName(lastName);
        if (email != null) existing.setEmail(email);
        if (phoneNumber != null) existing.setPhoneNumber(phoneNumber);
        if (profile != null) existing.setProfile(profile);
        if (status != null) existing.setStatus(status);
    }
}