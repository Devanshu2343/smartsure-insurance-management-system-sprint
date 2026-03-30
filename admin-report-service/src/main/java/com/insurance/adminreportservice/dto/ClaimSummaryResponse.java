package com.insurance.adminreportservice.dto;

import lombok.Getter;

/**
 * Claim summary DTO received from Claim Service.
 */
@Getter
public class ClaimSummaryResponse {

    private long totalClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long underReviewClaims;
}
