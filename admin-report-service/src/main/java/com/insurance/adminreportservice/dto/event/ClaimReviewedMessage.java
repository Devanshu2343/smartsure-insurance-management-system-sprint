package com.insurance.adminreportservice.dto.event;

import java.time.LocalDateTime;

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
}
