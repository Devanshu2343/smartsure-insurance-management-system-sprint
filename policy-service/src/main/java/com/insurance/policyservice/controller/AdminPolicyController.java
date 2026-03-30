package com.insurance.policyservice.controller;

import com.insurance.policyservice.dto.PolicyResponse;
import com.insurance.policyservice.dto.PolicySummaryResponse;
import com.insurance.policyservice.dto.PurchasePolicyRequest;
import com.insurance.policyservice.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative policy management endpoints.
 */
@RestController
@RequestMapping("/api/admin/policies")
@RequiredArgsConstructor
public class AdminPolicyController {

    private final PolicyService policyService;

    @PostMapping
    public ResponseEntity<PolicyResponse> createPolicy(
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.createPolicyForAdmin(request, userRole));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable("id") Long policyId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseEntity.ok(policyService.updatePolicyForAdmin(policyId, request, userRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(
            @PathVariable("id") Long policyId,
            @RequestHeader("X-User-Role") String userRole) {
        policyService.deletePolicyForAdmin(policyId, userRole);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<PolicySummaryResponse> getPolicySummary(
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(policyService.getPolicySummary(userRole));
    }
}
