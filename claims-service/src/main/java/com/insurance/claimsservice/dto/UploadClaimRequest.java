package com.insurance.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadClaimRequest {

    @NotNull(message = "Claim id is required")
    private Long claimId;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;
}
