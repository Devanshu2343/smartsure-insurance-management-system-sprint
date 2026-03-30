package com.insurance.policyservice.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Summary metrics for policies used by admin reporting.
 */
@Getter
@Builder
public class PolicySummaryResponse {

    private final long totalPolicies;
    private final long activePolicies;
    private final long expiredPolicies;
}
