package com.insurance.claimsservice.controller;

import com.insurance.claimsservice.dto.ClaimResponse;
import com.insurance.claimsservice.dto.ClaimSummaryResponse;
import com.insurance.claimsservice.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative claim review endpoints.
 */
@RestController
@RequestMapping("/api/admin/claims")
@RequiredArgsConstructor
public class AdminClaimController {

    private final ClaimService claimService;

    @PutMapping("/{id}/review")
    public ResponseEntity<ClaimResponse> reviewClaim(
            @PathVariable("id") Long claimId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam("decision") String decision,
            @RequestParam(value = "remarks", required = false) String remarks) {
        return ResponseEntity.ok(claimService.reviewClaim(claimId, decision, remarks, userRole));
    }

    @GetMapping("/summary")
    public ResponseEntity<ClaimSummaryResponse> getClaimSummary(
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(claimService.getClaimSummary(userRole));
    }
}
