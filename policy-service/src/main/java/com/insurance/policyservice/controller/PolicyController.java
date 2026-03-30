package com.insurance.policyservice.controller;

import com.insurance.policyservice.dto.PolicyResponse;
import com.insurance.policyservice.dto.PurchasePolicyRequest;
import com.insurance.policyservice.service.PolicyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-facing policy endpoints.
 */
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/purchase")
    public ResponseEntity<PolicyResponse> purchasePolicy(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.purchasePolicy(request, userEmail, userRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicyById(
            @PathVariable("id") Long policyId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(policyService.getPolicyById(policyId, userEmail, userRole));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(policyService.getPoliciesForCustomer(userEmail, userRole));
    }
}
