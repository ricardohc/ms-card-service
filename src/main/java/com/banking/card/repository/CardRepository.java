package com.banking.card.repository;

import com.banking.card.document.CardDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CardRepository extends ReactiveCrudRepository<CardDocument, UUID> {

    Flux<CardDocument> findAllBy(Pageable pageable);

    Mono<Long> countByStatusNot(String status);

    Flux<CardDocument> findByCustomerId(UUID customerId);

    Flux<CardDocument> findByCustomerIdAndTypeAndStatus(UUID customerId, String type, String status);

    Mono<CardDocument> findByIdAndStatusNot(UUID id, String status);

    Mono<CardDocument> findByLinkedAccountIdAndStatusNot(UUID linkedAccountId, String status);

    Mono<Boolean> existsByCustomerIdAndTypeAndStatus(UUID customerId, String type, String status);
}