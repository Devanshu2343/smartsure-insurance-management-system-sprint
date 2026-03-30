package com.insurance.policyservice.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Envelope that carries policy.created events with versioned metadata.
 */
public record PolicyCreatedMessage(
        String eventId,
        String eventType,
        int schemaVersion,
        String idempotencyKey,
        String traceId,
        String source,
        LocalDateTime occurredAt,
        PolicyCreatedEvent payload) {

    public static final int VERSION = 1;
    private static final String EVENT_TYPE = "policy.created";

    public static PolicyCreatedMessage from(PolicyCreatedEvent payload) {
        String traceId = MDC.get("traceId");
        return new PolicyCreatedMessage(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                VERSION,
                "policy-" + payload.policyId(),
                traceId == null ? "N/A" : traceId,
                "policy-service",
                LocalDateTime.now(),
                payload
        );
    }
}
