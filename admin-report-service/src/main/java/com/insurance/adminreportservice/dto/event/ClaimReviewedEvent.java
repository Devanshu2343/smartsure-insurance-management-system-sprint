package com.insurance.adminreportservice.dto.event;

import java.time.LocalDateTime;

/**
 * Event consumed when a claim is approved or rejected.
 */
public record ClaimReviewedEvent(
        Long claimId,
        String claimNumber,
        String status,
        String decision,
        LocalDateTime reviewedAt) {
}
