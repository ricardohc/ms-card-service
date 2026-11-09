package com.banking.card.repository;

import com.banking.card.document.CardDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CardAtomicOperations {

    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<Boolean> decrementAvailableBalance(UUID cardId, BigDecimal amount) {
        Decimal128 delta = new Decimal128(amount.negate());
        Decimal128 min = new Decimal128(amount);

        Query query = new Query(new Criteria().andOperator(
                Criteria.where("_id").is(cardId),
                Criteria.where("availableBalance").gte(min)));

        Update update = new Update().inc("availableBalance", delta);

        return mongoTemplate.findAndModify(query, update, CardDocument.class)
                .map(updated -> {
                    log.debug("availableBalance decremented by {} for card {}", amount, cardId);
                    return true;
                })
                .defaultIfEmpty(false)
                .doOnError(ex -> log.error(
                        "Atomic decrement failed for card {} amount {}: {}",
                        cardId, amount, ex.getMessage()));
    }
}