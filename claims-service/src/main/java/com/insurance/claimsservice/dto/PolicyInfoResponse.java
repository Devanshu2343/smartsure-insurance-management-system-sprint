package com.insurance.claimsservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyInfoResponse(
        Long id,
        String policyNumber,
        String customerEmail,
        String status) {
}
