package com.banking.card.repository;

import com.banking.card.document.MovementDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface MovementRepository extends ReactiveCrudRepository<MovementDocument, UUID> {

    Flux<MovementDocument> findTop10ByCardIdOrderByTimestampDesc(UUID cardId);
}

