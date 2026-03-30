package com.insurance.claimsservice.exception;

/**
 * Thrown when a claim operation is requested for a non-registered user.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
