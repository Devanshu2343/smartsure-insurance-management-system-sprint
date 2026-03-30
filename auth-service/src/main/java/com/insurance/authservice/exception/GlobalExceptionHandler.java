package com.insurance.authservice.exception;

import com.insurance.authservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        String message = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .distinct()
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

}
