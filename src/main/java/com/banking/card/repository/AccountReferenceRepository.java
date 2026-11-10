package com.banking.card.repository;

import com.banking.card.document.AccountReferenceDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountReferenceRepository extends ReactiveMongoRepository<AccountReferenceDocument, String> {
}