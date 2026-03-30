package com.insurance.claimsservice.dto.event;

import java.time.LocalDateTime;

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
}
