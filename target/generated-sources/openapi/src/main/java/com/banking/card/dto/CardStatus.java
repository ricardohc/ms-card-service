package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets CardStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T11:44:42.467727-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public enum CardStatus {
  
  ACTIVE("ACTIVE"),
  
  BLOCKED("BLOCKED"),
  
  EXPIRED("EXPIRED"),
  
  CANCELLED("CANCELLED");

  private String value;

  CardStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static CardStatus fromValue(String value) {
    for (CardStatus b : CardStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

