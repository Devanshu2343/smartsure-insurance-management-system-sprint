package com.insurance.adminreportservice.dto.event;

import java.time.LocalDateTime;

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
}
