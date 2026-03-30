package com.insurance.adminreportservice.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

    @Getter
    @Builder
    public class ReportResponse {

    private final long totalPolicies;
    private final long activePolicies;
    private final long totalReviewedClaims;
    private final long approvedClaims;
    private final long rejectedClaims;
    private final LocalDateTime generatedAt;
}
