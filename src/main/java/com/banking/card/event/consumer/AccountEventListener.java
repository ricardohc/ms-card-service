package com.banking.card.event.consumer;

import com.banking.card.document.AccountReferenceDocument;
import com.banking.card.repository.AccountReferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventListener {

    private final AccountReferenceRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "banking.account.events.v1", groupId = "card-service-group")
    public void listenAccountEvents(@Payload String message) {
        log.info("Message received from banking.account.events.v1: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();
            JsonNode payload = root.path("payload");

            if (payload.isMissingNode() || payload.path("id").isMissingNode()) {
                log.warn("Evento inválido recibido (sin payload o id): {}", message);
                return;
            }

            String accountId = payload.path("id").asText();
            log.info("Processing eventType={} for accountId={}", eventType, accountId);

            switch (eventType) {
                case "i" -> insertAccount(accountId, payload);
                case "u" -> updateAccount(accountId, payload);
                case "d" -> deleteAccount(accountId);
                default -> log.debug("Evento no manejado: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error procesando evento de cuenta: {}", message, e);
        }
    }

    private void insertAccount(String accountId, JsonNode payload) {
        AccountReferenceDocument doc = AccountReferenceDocument.builder()
                .id(accountId)
                .accountNumber(payload.path("accountNumber").asText(null))
                .customerId(payload.path("customerId").asText())
                .accountType(payload.path("accountType").asText(null))
                .profile(payload.path("profile").asText(null))
                .status(payload.path("status").asText("ACTIVE"))
                .balance(new BigDecimal(payload.path("balance").asText("0.00")))
                .currency(payload.path("currency").asText(null))
                .createdAt(parseInstant(payload.path("createdAt").asText(null)))
                .updatedAt(Instant.now())
                .build();

        log.info("Saving new account reference for {}", accountId);
        repository.save(doc).subscribe(
                saved -> log.info("Account reference insertado para {}", accountId),
                error -> log.error("Error insertando account reference {}", accountId, error)
        );
    }

    private void updateAccount(String accountId, JsonNode payload) {
        repository.findById(accountId)
                .flatMap(doc -> {
                    applyPayload(doc, payload);
                    doc.setUpdatedAt(Instant.now());
                    log.info("Updating account reference for {}", accountId);
                    return repository.save(doc);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    AccountReferenceDocument doc = AccountReferenceDocument.builder()
                            .id(accountId)
                            .accountNumber(payload.path("accountNumber").asText(null))
                            .customerId(payload.path("customerId").asText())
                            .accountType(payload.path("accountType").asText(null))
                            .profile(payload.path("profile").asText(null))
                            .status(payload.path("status").asText(null))
                            .balance(new BigDecimal(payload.path("balance").asText("0.00")))
                            .currency(payload.path("currency").asText(null))
                            .createdAt(parseInstant(payload.path("createdAt").asText(null)))
                            .updatedAt(Instant.now())
                            .build();
                    log.info("Creating missing account reference during update for {}", accountId);
                    return repository.save(doc);
                }))
                .subscribe(
                        saved -> log.info("Account reference actualizado para {}", accountId),
                        error -> log.error("Error actualizando account reference {}", accountId, error)
                );
    }

    private void deleteAccount(String accountId) {
        repository.findById(accountId)
                .flatMap(doc -> {
                    doc.setStatus("INACTIVE");
                    doc.setUpdatedAt(Instant.now());
                    log.info("Deactivating account reference for {}", accountId);
                    return repository.save(doc);
                })
                .subscribe(
                        saved -> log.info("Account reference desactivado para {}", accountId),
                        error -> log.error("Error desactivando account reference {}", accountId, error)
                );
    }

    private void applyPayload(AccountReferenceDocument existing, JsonNode payload) {
        String accountNumber = payload.path("accountNumber").asText(null);
        String accountType = payload.path("accountType").asText(null);
        String profile = payload.path("profile").asText(null);
        String status = payload.path("status").asText(null);
        String balance = payload.path("balance").asText(null);
        String currency = payload.path("currency").asText(null);

        if (accountNumber != null) existing.setAccountNumber(accountNumber);
        if (accountType != null) existing.setAccountType(accountType);
        if (profile != null) existing.setProfile(profile);
        if (status != null) existing.setStatus(status);
        if (balance != null) existing.setBalance(new BigDecimal(balance));
        if (currency != null) existing.setCurrency(currency);
    }

    private Instant parseInstant(String value) {
        if (value == null) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}