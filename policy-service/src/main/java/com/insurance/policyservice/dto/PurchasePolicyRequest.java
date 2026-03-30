package com.insurance.policyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchasePolicyRequest {

    @NotBlank(message = "Policy type is required")
    private String policyType;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", message = "Coverage amount must be greater than zero")
    private BigDecimal coverageAmount;

    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "0.01", message = "Premium amount must be greater than zero")
    private BigDecimal premiumAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;
}
