package com.banking.card.exception;

import com.banking.card.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

/** Maps domain exceptions to the ErrorResponse contract of the OpenAPI spec. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomain(DomainException ex) {
        log.warn("Domain error code={} message={}", ex.getCode(), ex.getMessage());
        return Mono.just(build(HttpStatus.valueOf(ex.getStatus()), ex.getCode(), ex.getMessage(), List.of(ex.getMessage())));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInput(ServerWebInputException ex) {
        return Mono.just(build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getReason(), List.of(ex.getReason())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected internal error", List.of(ex.getClass().getSimpleName())));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, List<String> details) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(code);
        error.setMessage(message);
        error.setTimestamp(OffsetDateTime.now());
        error.setCorrelationId(null);
        error.setDetails(details);
        return ResponseEntity.status(status).body(error);
    }
}
