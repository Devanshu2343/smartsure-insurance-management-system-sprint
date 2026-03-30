package com.insurance.policyservice.dto;

import com.insurance.policyservice.entity.PolicyStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolicyResponse {

    private final Long id;
    private final String policyNumber;
    private final String customerEmail;
    private final String policyType;
    private final BigDecimal coverageAmount;
    private final BigDecimal premiumAmount;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final PolicyStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
