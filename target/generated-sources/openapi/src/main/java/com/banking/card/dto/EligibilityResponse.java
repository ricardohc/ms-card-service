package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * EligibilityResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-06T11:39:44.157711600-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class EligibilityResponse {

  private UUID customerId;

  private Boolean hasCreditCard;

  public EligibilityResponse customerId(UUID customerId) {
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

  public EligibilityResponse hasCreditCard(Boolean hasCreditCard) {
    this.hasCreditCard = hasCreditCard;
    return this;
  }

  /**
   * Get hasCreditCard
   * @return hasCreditCard
  */
  
  @Schema(name = "hasCreditCard", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hasCreditCard")
  public Boolean getHasCreditCard() {
    return hasCreditCard;
  }

  public void setHasCreditCard(Boolean hasCreditCard) {
    this.hasCreditCard = hasCreditCard;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EligibilityResponse eligibilityResponse = (EligibilityResponse) o;
    return Objects.equals(this.customerId, eligibilityResponse.customerId) &&
        Objects.equals(this.hasCreditCard, eligibilityResponse.hasCreditCard);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId, hasCreditCard);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EligibilityResponse {\n");
    sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
    sb.append("    hasCreditCard: ").append(toIndentedString(hasCreditCard)).append("\n");
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

