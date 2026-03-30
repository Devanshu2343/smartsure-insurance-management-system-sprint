package com.insurance.claimsservice.service;

/**
 * Persists audit events for observability and compliance.
 */
public interface AuditEventService {

    void record(String eventType, Object eventPayload);
}
