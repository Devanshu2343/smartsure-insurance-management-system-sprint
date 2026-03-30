package com.insurance.claimsservice.controller;

import com.insurance.claimsservice.dto.ClaimResponse;
import com.insurance.claimsservice.dto.ClaimStatusResponse;
import com.insurance.claimsservice.dto.InitiateClaimRequest;
import com.insurance.claimsservice.dto.UploadClaimRequest;
import com.insurance.claimsservice.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer claim submission and tracking endpoints.
 */
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping("/initiate")
    public ResponseEntity<ClaimResponse> initiateClaim(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody InitiateClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.initiateClaim(request, userEmail, userRole));
    }

    @PostMapping("/upload")
    public ResponseEntity<ClaimResponse> uploadClaimDocument(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody UploadClaimRequest request) {
        return ResponseEntity.ok(claimService.uploadClaimDocument(request, userEmail, userRole));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ClaimStatusResponse> getClaimStatus(
            @PathVariable("id") Long claimId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(claimService.getClaimStatus(claimId, userEmail, userRole));
    }

}
