package com.insurance.claimsservice.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Envelope that carries claim.submitted events with versioned metadata.
 */
public record ClaimSubmittedMessage(
        String eventId,
        String eventType,
        int schemaVersion,
        String idempotencyKey,
        String traceId,
        String source,
        LocalDateTime occurredAt,
        ClaimSubmittedEvent payload) {

    public static final int VERSION = 1;
    private static final String EVENT_TYPE = "claim.submitted";

    public static ClaimSubmittedMessage from(ClaimSubmittedEvent payload) {
        String traceId = MDC.get("traceId");
        return new ClaimSubmittedMessage(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                VERSION,
                "claim-" + payload.claimId() + "-submitted",
                traceId == null ? "N/A" : traceId,
                "claims-service",
                LocalDateTime.now(),
                payload
        );
    }
}
