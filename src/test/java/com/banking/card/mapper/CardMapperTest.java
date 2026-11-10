package com.banking.card.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.banking.card.document.CardDocument;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CardMapperTest {

    private final CardMapper mapper = new CardMapper();

    @Test
    void toResponseMapsCreditCardFully() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        LocalDate expiration = LocalDate.of(2028, 12, 31);

        CardDocument doc = CardDocument.builder()
                .id(id)
                .customerId(customerId)
                .type("CREDIT")
                .status("ACTIVE")
                .maskedPan("**** **** **** 1234")
                .linkedAccountId(accountId)
                .creditLimit(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("4200.50"))
                .currency("PEN")
                .expirationDate(expiration)
                .build();

        CardResponse response = mapper.toResponse(doc);

        assertEquals(id, response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(CardType.CREDIT, response.getType());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals("**** **** **** 1234", response.getMaskedPan());
        assertEquals(accountId, response.getLinkedAccountId());
        assertEquals("5000.00", response.getCreditLimit());
        assertEquals("4200.50", response.getAvailableBalance());
        assertEquals(expiration, response.getExpirationDate());
    }

    @Test
    void toResponseMapsDebitCardWithNullCreditFields() {
        CardDocument doc = CardDocument.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type("DEBIT")
                .status("BLOCKED")
                .maskedPan("**** **** **** 9999")
                .linkedAccountId(UUID.randomUUID())
                .creditLimit(null)
                .availableBalance(null)
                .currency("PEN")
                .expirationDate(LocalDate.of(2027, 6, 30))
                .build();

        CardResponse response = mapper.toResponse(doc);

        assertEquals(CardType.DEBIT, response.getType());
        assertEquals(CardStatus.BLOCKED, response.getStatus());
        assertNull(response.getCreditLimit());
        assertNull(response.getAvailableBalance());
    }

    @Test
    void toResponseMapsCancelledStatus() {
        CardDocument doc = CardDocument.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type("CREDIT")
                .status("CANCELLED")
                .maskedPan("**** **** **** 0001")
                .creditLimit(new BigDecimal("1000.00"))
                .availableBalance(BigDecimal.ZERO)
                .currency("PEN")
                .expirationDate(LocalDate.now())
                .build();

        CardResponse response = mapper.toResponse(doc);

        assertEquals(CardStatus.CANCELLED, response.getStatus());
        assertEquals("0", response.getAvailableBalance());
    }

    @Test
    void toResponsePreservesDecimalScale() {
        CardDocument doc = CardDocument.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type("CREDIT")
                .status("ACTIVE")
                .maskedPan("**** **** **** 5555")
                .creditLimit(new BigDecimal("1234.56"))
                .availableBalance(new BigDecimal("0.00"))
                .currency("PEN")
                .expirationDate(LocalDate.now())
                .build();

        CardResponse response = mapper.toResponse(doc);

        assertEquals("1234.56", response.getCreditLimit());
        assertEquals("0.00", response.getAvailableBalance());
    }
}