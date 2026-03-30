package com.insurance.claimsservice.dto;

import com.insurance.claimsservice.entity.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimResponse {

    private final Long id;
    private final String claimNumber;
    private final Long policyId;
    private final String customerEmail;
    private final BigDecimal claimAmount;
    private final String description;
    private final String documentUrl;
    private final ClaimStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
