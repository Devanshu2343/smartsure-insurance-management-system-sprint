package com.insurance.adminreportservice.dto;

import lombok.Getter;

/**
 * Policy summary DTO received from Policy Service.
 */
@Getter
public class PolicySummaryResponse {

    private long totalPolicies;
    private long activePolicies;
    private long expiredPolicies;
}
