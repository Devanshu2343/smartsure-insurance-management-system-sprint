package com.insurance.claimsservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitiateClaimRequest {

    @NotNull(message = "Policy id is required")
    private Long policyId;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", message = "Claim amount must be greater than zero")
    private BigDecimal claimAmount;

    @NotBlank(message = "Claim description is required")
    private String description;

    private String customerEmail;
}
