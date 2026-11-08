package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConsumptionRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T13:11:23.161203400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class ConsumptionRequest {

  private String amount;

  private String currency;

  private String merchant;

  public ConsumptionRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ConsumptionRequest(String amount, String currency, String merchant) {
    this.amount = amount;
    this.currency = currency;
    this.merchant = merchant;
  }

  public ConsumptionRequest amount(String amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  */
  @NotNull @Pattern(regexp = "^(?!0+(\\.0+)?$)[0-9]+(\\.[0-9]{1,2})?$") 
  @Schema(name = "amount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amount")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public ConsumptionRequest currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  @NotNull @Pattern(regexp = "^[A-Z]{3}$") 
  @Schema(name = "currency", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public ConsumptionRequest merchant(String merchant) {
    this.merchant = merchant;
    return this;
  }

  /**
   * Get merchant
   * @return merchant
  */
  @NotNull 
  @Schema(name = "merchant", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("merchant")
  public String getMerchant() {
    return merchant;
  }

  public void setMerchant(String merchant) {
    this.merchant = merchant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumptionRequest consumptionRequest = (ConsumptionRequest) o;
    return Objects.equals(this.amount, consumptionRequest.amount) &&
        Objects.equals(this.currency, consumptionRequest.currency) &&
        Objects.equals(this.merchant, consumptionRequest.merchant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency, merchant);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumptionRequest {\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    merchant: ").append(toIndentedString(merchant)).append("\n");
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

