package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * DebitCardCreateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T11:44:42.467727-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class DebitCardCreateRequest implements CardCreateRequest {

  private UUID customerId;

  private String type;

  private UUID linkedAccountId;

  public DebitCardCreateRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DebitCardCreateRequest(UUID customerId, String type, UUID linkedAccountId) {
    this.customerId = customerId;
    this.type = type;
    this.linkedAccountId = linkedAccountId;
  }

  public DebitCardCreateRequest customerId(UUID customerId) {
    this.customerId = customerId;
    return this;
  }

  /**
   * Get customerId
   * @return customerId
  */
  @NotNull @Valid 
  @Schema(name = "customerId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("customerId")
  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public DebitCardCreateRequest type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  @NotNull 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public DebitCardCreateRequest linkedAccountId(UUID linkedAccountId) {
    this.linkedAccountId = linkedAccountId;
    return this;
  }

  /**
   * Get linkedAccountId
   * @return linkedAccountId
  */
  @NotNull @Valid 
  @Schema(name = "linkedAccountId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("linkedAccountId")
  public UUID getLinkedAccountId() {
    return linkedAccountId;
  }

  public void setLinkedAccountId(UUID linkedAccountId) {
    this.linkedAccountId = linkedAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DebitCardCreateRequest debitCardCreateRequest = (DebitCardCreateRequest) o;
    return Objects.equals(this.customerId, debitCardCreateRequest.customerId) &&
        Objects.equals(this.type, debitCardCreateRequest.type) &&
        Objects.equals(this.linkedAccountId, debitCardCreateRequest.linkedAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId, type, linkedAccountId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DebitCardCreateRequest {\n");
    sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    linkedAccountId: ").append(toIndentedString(linkedAccountId)).append("\n");
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

