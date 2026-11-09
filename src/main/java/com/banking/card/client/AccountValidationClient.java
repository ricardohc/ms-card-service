package com.banking.card.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Validates linked account existence and ACTIVE status against ms-account-service. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountValidationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.services.account.url:http://ms-account-service}")
    private String accountServiceUrl;

    /** Returns true when the account exists and is ACTIVE. */
    public Mono<Boolean> accountActive(UUID accountId) {
        return webClientBuilder.build()
                .get()
                .uri(accountServiceUrl + "/api/v1/accounts/{accountId}", accountId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Channel-ID", "MS-CARD-SERVICE")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> body != null && body.contains("ACTIVE"))
                .onErrorResume(ex -> {
                    log.warn("Account validation failed for {}: {}", accountId, ex.getMessage());
                    return Mono.just(false);
                });
    }
}