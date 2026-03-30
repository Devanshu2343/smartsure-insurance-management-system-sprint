package com.insurance.adminreportservice.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event consumed when a claim is submitted.
 */
public record ClaimSubmittedEvent(
        Long claimId,
        String claimNumber,
        Long policyId,
        String customerEmail,
        BigDecimal claimAmount,
        String status,
        LocalDateTime submittedAt) {
}
