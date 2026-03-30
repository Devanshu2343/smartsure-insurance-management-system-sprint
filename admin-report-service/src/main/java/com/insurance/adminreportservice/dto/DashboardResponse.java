package com.insurance.adminreportservice.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * Aggregated admin dashboard metrics.
 */
@Getter
@Builder
public class DashboardResponse {

    private final long totalPolicies;
    private final long activePolicies;
    private final long expiredPolicies;
    private final long totalClaims;
    private final long approvedClaims;
    private final long rejectedClaims;
    private final long underReviewClaims;
    private final LocalDateTime generatedAt;
}
