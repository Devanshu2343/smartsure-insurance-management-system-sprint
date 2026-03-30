package com.insurance.policyservice.dto.event;

import com.insurance.policyservice.entity.Policy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event published when a policy is created.
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

    public static PolicyCreatedEvent from(Policy policy) {
        return new PolicyCreatedEvent(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getCustomerEmail(),
                policy.getStatus().name(),
                policy.getCoverageAmount(),
                policy.getPremiumAmount(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getCreatedAt()
        );
    }
}
