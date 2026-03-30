package com.insurance.adminreportservice.service;

/**
 * Tracks processed event keys for idempotency.
 */
public interface IdempotencyService {

    boolean isDuplicate(String idempotencyKey);
}
