package com.insurance.claimsservice.dto;

import com.insurance.claimsservice.entity.ClaimStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimStatusResponse {

    private final Long claimId;
    private final String claimNumber;
    private final ClaimStatus status;
    private final LocalDateTime updatedAt;
}
