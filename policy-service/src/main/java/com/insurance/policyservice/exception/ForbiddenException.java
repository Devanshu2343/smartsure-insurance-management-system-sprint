package com.insurance.policyservice.exception;

/**
 * Thrown when a user attempts an operation without required permissions.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
