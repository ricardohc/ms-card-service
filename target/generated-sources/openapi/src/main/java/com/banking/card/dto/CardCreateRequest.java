package com.banking.card.dto;

import java.net.URI;
import java.util.Objects;
import com.banking.card.dto.CreditCardCreateRequest;
import com.banking.card.dto.DebitCardCreateRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;


@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-06T11:39:44.157711600-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public interface CardCreateRequest {
}
