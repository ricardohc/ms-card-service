package com.banking.card.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cards")
@CompoundIndex(name = "idx_customer_type_status", def = "{'customerId': 1, 'type': 1, 'status': 1}")
public class CardDocument {

    @Id
    private UUID id;

    @Indexed
    private UUID customerId;

    /** CREDIT or DEBIT. */
    private String type;

    /** ACTIVE, BLOCKED, EXPIRED, CANCELLED. */
    private String status;

    /** Masked PAN only; full PAN is never stored. */
    private String maskedPan;

    private UUID linkedAccountId;

    private BigDecimal creditLimit;

    private BigDecimal availableBalance;

    private String currency;

    private LocalDate expirationDate;

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
