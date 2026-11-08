package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.banking.card.dto.CardStatus;
import com.banking.card.dto.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CardResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T13:29:33.083421400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class CardResponse {

  private UUID id;

  private UUID customerId;

  private CardType type;

  private CardStatus status;

  private String maskedPan;

  private UUID linkedAccountId;

  private String creditLimit;

  private String availableBalance;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate expirationDate;

  public CardResponse id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public CardResponse customerId(UUID customerId) {
    this.customerId = customerId;
    return this;
  }

  /**
   * Get customerId
   * @return customerId
  */
  @Valid 
  @Schema(name = "customerId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("customerId")
  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public CardResponse type(CardType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public CardType getType() {
    return type;
  }

  public void setType(CardType type) {
    this.type = type;
  }

  public CardResponse status(CardStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public CardStatus getStatus() {
    return status;
  }

  public void setStatus(CardStatus status) {
    this.status = status;
  }

  public CardResponse maskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
    return this;
  }

  /**
   * Get maskedPan
   * @return maskedPan
  */
  
  @Schema(name = "maskedPan", example = "**** **** **** 1234", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maskedPan")
  public String getMaskedPan() {
    return maskedPan;
  }

  public void setMaskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
  }

  public CardResponse linkedAccountId(UUID linkedAccountId) {
    this.linkedAccountId = linkedAccountId;
    return this;
  }

  /**
   * Get linkedAccountId
   * @return linkedAccountId
  */
  @Valid 
  @Schema(name = "linkedAccountId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("linkedAccountId")
  public UUID getLinkedAccountId() {
    return linkedAccountId;
  }

  public void setLinkedAccountId(UUID linkedAccountId) {
    this.linkedAccountId = linkedAccountId;
  }

  public CardResponse creditLimit(String creditLimit) {
    this.creditLimit = creditLimit;
    return this;
  }

  /**
   * Get creditLimit
   * @return creditLimit
  */
  
  @Schema(name = "creditLimit", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("creditLimit")
  public String getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(String creditLimit) {
    this.creditLimit = creditLimit;
  }

  public CardResponse availableBalance(String availableBalance) {
    this.availableBalance = availableBalance;
    return this;
  }

  /**
   * Get availableBalance
   * @return availableBalance
  */
  
  @Schema(name = "availableBalance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("availableBalance")
  public String getAvailableBalance() {
    return availableBalance;
  }

  public void setAvailableBalance(String availableBalance) {
    this.availableBalance = availableBalance;
  }

  public CardResponse expirationDate(LocalDate expirationDate) {
    this.expirationDate = expirationDate;
    return this;
  }

  /**
   * Get expirationDate
   * @return expirationDate
  */
  @Valid 
  @Schema(name = "expirationDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expirationDate")
  public LocalDate getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDate expirationDate) {
    this.expirationDate = expirationDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardResponse cardResponse = (CardResponse) o;
    return Objects.equals(this.id, cardResponse.id) &&
        Objects.equals(this.customerId, cardResponse.customerId) &&
        Objects.equals(this.type, cardResponse.type) &&
        Objects.equals(this.status, cardResponse.status) &&
        Objects.equals(this.maskedPan, cardResponse.maskedPan) &&
        Objects.equals(this.linkedAccountId, cardResponse.linkedAccountId) &&
        Objects.equals(this.creditLimit, cardResponse.creditLimit) &&
        Objects.equals(this.availableBalance, cardResponse.availableBalance) &&
        Objects.equals(this.expirationDate, cardResponse.expirationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, customerId, type, status, maskedPan, linkedAccountId, creditLimit, availableBalance, expirationDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
    sb.append("    linkedAccountId: ").append(toIndentedString(linkedAccountId)).append("\n");
    sb.append("    creditLimit: ").append(toIndentedString(creditLimit)).append("\n");
    sb.append("    availableBalance: ").append(toIndentedString(availableBalance)).append("\n");
    sb.append("    expirationDate: ").append(toIndentedString(expirationDate)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

