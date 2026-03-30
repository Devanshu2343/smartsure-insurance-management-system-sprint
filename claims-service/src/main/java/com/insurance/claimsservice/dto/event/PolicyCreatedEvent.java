package com.insurance.claimsservice.dto.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event consumed when a policy is created.
 */
public record PolicyCreatedEvent(
        Long policyId,
        String policyNumber,
        String customerEmail,
        String status,
        BigDecimal coverageAmount,
        BigDecimal premiumAmount,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt) {
}
