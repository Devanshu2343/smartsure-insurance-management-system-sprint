package com.insurance.adminreportservice.exception;

/**
 * Thrown when a request lacks administrative permissions.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
