package com.insurance.adminreportservice.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
