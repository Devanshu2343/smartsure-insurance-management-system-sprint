package com.insurance.claimsservice.exception;

/**
 * Thrown when user role or ownership restrictions are violated.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
