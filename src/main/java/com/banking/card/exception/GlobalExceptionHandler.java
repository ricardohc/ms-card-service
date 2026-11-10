package com.banking.card.exception;

import com.banking.card.dto.ErrorResponse;
import com.banking.card.util.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomain(DomainException ex, ServerWebExchange exchange) {
        log.warn("Domain error code={} message={}", ex.getCode(), ex.getMessage());
        return Mono.just(build(HttpStatus.valueOf(ex.getStatus()), ex.getCode(), ex.getMessage(), exchange));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInput(ServerWebInputException ex, ServerWebExchange exchange) {
        return Mono.just(build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getReason(), exchange));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        return Mono.just(build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected internal error", exchange));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, ServerWebExchange exchange) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(code);
        error.setMessage(message);
        error.setTimestamp(OffsetDateTime.now());

        String corrId = exchange.getRequest().getHeaders().getFirst(AppConstants.HEADER_CORRELATION_ID);
        error.setCorrelationId(corrId);

        error.setDetails(List.of(message));
        return ResponseEntity.status(status).body(error);
    }
}