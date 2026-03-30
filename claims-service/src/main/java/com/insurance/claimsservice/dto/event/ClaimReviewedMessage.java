package com.insurance.claimsservice.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Envelope that carries claim.reviewed events with versioned metadata.
 */
public record ClaimReviewedMessage(
        String eventId,
        String eventType,
        int schemaVersion,
        String idempotencyKey,
        String traceId,
        String source,
        LocalDateTime occurredAt,
        ClaimReviewedEvent payload) {

    public static final int VERSION = 1;
    private static final String EVENT_TYPE = "claim.reviewed";

    public static ClaimReviewedMessage from(ClaimReviewedEvent payload) {
        String traceId = MDC.get("traceId");
        return new ClaimReviewedMessage(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                VERSION,
                "claim-" + payload.claimId() + "-reviewed",
                traceId == null ? "N/A" : traceId,
                "claims-service",
                LocalDateTime.now(),
                payload
        );
    }
}
