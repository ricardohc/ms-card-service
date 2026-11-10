package com.banking.card.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardEvent {

    public static final String INSERT = "i";
    public static final String UPDATE = "u";
    public static final String DELETE = "d";

    private UUID eventId;
    private String eventType;
    private Instant occurredAt;
    private String correlationId;
    private CardPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardPayload {
        private UUID id;
        private UUID customerId;
        private String type;
        private String status;
        private String maskedPan;
        private UUID linkedAccountId;
        private String creditLimit;
        private String availableBalance;
        private String expirationDate;
    }
}