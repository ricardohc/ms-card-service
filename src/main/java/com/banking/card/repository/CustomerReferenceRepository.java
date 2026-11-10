package com.banking.card.repository;

import com.banking.card.document.CustomerReferenceDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerReferenceRepository extends ReactiveMongoRepository<CustomerReferenceDocument, String> {
}