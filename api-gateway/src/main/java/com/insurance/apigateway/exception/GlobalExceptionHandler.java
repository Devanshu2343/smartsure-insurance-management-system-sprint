package com.insurance.apigateway.exception;

import com.insurance.apigateway.dto.ErrorResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ErrorResponse> handleGatewayException(
            GatewayException exception,
            ServerWebExchange exchange) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(exception.getMessage())
                .path(exchange.getRequest().getURI().getPath())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
