package com.banking.card.mapper;

import com.banking.card.document.CardDocument;
import com.banking.card.document.MovementDocument;
import com.banking.card.dto.CardResponse;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardType;
import com.banking.card.dto.MovementListResponse;
import com.banking.card.dto.MovementListResponseMovementsInner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps MongoDB documents to contract DTOs (never the reverse). */
@Component
public class CardMapper {

    public CardResponse toResponse(CardDocument doc) {
        CardResponse response = new CardResponse();
        response.setId(doc.getId());
        response.setCustomerId(doc.getCustomerId());
        response.setType(toCardType(doc.getType()));
        response.setStatus(toCardStatus(doc.getStatus()));
        response.setMaskedPan(doc.getMaskedPan());
        response.setLinkedAccountId(doc.getLinkedAccountId());
        response.setCreditLimit(doc.getCreditLimit() == null ? null : doc.getCreditLimit().toPlainString());
        response.setAvailableBalance(doc.getAvailableBalance() == null ? null : doc.getAvailableBalance().toPlainString());
        response.setExpirationDate(doc.getExpirationDate());
        return response;
    }

    private static CardType toCardType(String value) {
        return CardType.fromValue(value);
    }

    private static CardStatus toCardStatus(String value) {
        return CardStatus.fromValue(value);
    }

    public MovementListResponse toMovementList(List<MovementDocument> docs) {
        MovementListResponse response = new MovementListResponse();
        List<MovementListResponseMovementsInner> items = docs.stream()
                .map(doc -> {
                    MovementListResponseMovementsInner item = new MovementListResponseMovementsInner();
                    item.setId(doc.getId());
                    item.setAmount(doc.getAmount() == null ? null : doc.getAmount().toPlainString());
                    item.setMerchant(doc.getMerchant());
                    item.setTimestamp(java.time.OffsetDateTime.ofInstant(doc.getTimestamp(), java.time.ZoneOffset.UTC));
                    return item;
                })
                .toList();
        response.setMovements(items);
        return response;
    }
}
