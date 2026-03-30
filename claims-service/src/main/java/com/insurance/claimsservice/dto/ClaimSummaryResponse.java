package com.insurance.claimsservice.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Summary metrics for claims used by admin reporting.
 */
@Getter
@Builder
public class ClaimSummaryResponse {

    private final long totalClaims;
    private final long approvedClaims;
    private final long rejectedClaims;
    private final long underReviewClaims;
}
