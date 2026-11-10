package com.banking.card.document;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account_reference")
public class AccountReferenceDocument {

    @Id
    private String id;

    private String accountNumber;
    private String customerId;
    private String accountType;
    private String profile;
    private String status;
    private BigDecimal balance;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;
}