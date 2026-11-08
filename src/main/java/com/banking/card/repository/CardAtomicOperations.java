package com.banking.card.repository;

import com.banking.card.document.CardDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/** Mongo-specific atomic operations that cannot be expressed as derived methods. */
@Repository
@RequiredArgsConstructor
public class CardAtomicOperations {

    private final ReactiveMongoTemplate mongoTemplate;

    /** Atomically decrements availableBalance only when the current balance covers the amount.
     *  Returns true when the decrement happened, false otherwise. */
    public Mono<Boolean> decrementAvailableBalance(UUID cardId, BigDecimal amount) {
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("_id").is(cardId),
                Criteria.where("availableBalance").gte(amount)));
        Update update = new Update().inc("availableBalance", amount.negate());
        return mongoTemplate.findAndModify(query, update, CardDocument.class)
                .map(updated -> true)
                .defaultIfEmpty(false);
    }
}
