package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.banking.card.dto.MovementListResponseMovementsInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MovementListResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T13:29:33.083421400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class MovementListResponse {

  @Valid
  private List<@Valid MovementListResponseMovementsInner> movements = new ArrayList<>();

  public MovementListResponse movements(List<@Valid MovementListResponseMovementsInner> movements) {
    this.movements = movements;
    return this;
  }

  public MovementListResponse addMovementsItem(MovementListResponseMovementsInner movementsItem) {
    if (this.movements == null) {
      this.movements = new ArrayList<>();
    }
    this.movements.add(movementsItem);
    return this;
  }

  /**
   * Get movements
   * @return movements
  */
  @Valid 
  @Schema(name = "movements", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("movements")
  public List<@Valid MovementListResponseMovementsInner> getMovements() {
    return movements;
  }

  public void setMovements(List<@Valid MovementListResponseMovementsInner> movements) {
    this.movements = movements;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MovementListResponse movementListResponse = (MovementListResponse) o;
    return Objects.equals(this.movements, movementListResponse.movements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(movements);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MovementListResponse {\n");
    sb.append("    movements: ").append(toIndentedString(movements)).append("\n");
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

