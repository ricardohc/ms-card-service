package com.banking.card.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Validates customer existence against ms-customer-service via its HTTP API. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerValidationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.services.customer.url:http://ms-customer-service}")
    private String customerServiceUrl;

    public Mono<Boolean> customerExists(UUID customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerServiceUrl + "/api/v1/customers/{customerId}", customerId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Channel-ID", "MS-CARD-SERVICE")
                .header("Accept", "application/json")
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorResume(ex -> {
                    log.warn("Customer validation failed for {}: {}", customerId, ex.getMessage());
                    return Mono.just(false);
                });
    }
}