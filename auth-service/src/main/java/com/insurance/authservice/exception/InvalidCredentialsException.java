package com.insurance.authservice.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
