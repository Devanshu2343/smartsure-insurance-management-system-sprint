package com.insurance.policyservice.exception;

/**
 * Thrown when authentication headers are missing.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
