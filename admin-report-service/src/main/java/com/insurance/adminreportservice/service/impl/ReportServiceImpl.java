package com.insurance.adminreportservice.service.impl;

import com.insurance.adminreportservice.client.ClaimServiceClient;
import com.insurance.adminreportservice.client.PolicyServiceClient;
import com.insurance.adminreportservice.dto.ClaimSummaryResponse;
import com.insurance.adminreportservice.dto.DashboardResponse;
import com.insurance.adminreportservice.dto.PolicySummaryResponse;
import com.insurance.adminreportservice.dto.ReportResponse;
import com.insurance.adminreportservice.exception.ForbiddenException;
import com.insurance.adminreportservice.exception.UnauthorizedException;
import com.insurance.adminreportservice.service.ReportService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final PolicyServiceClient policyServiceClient;
    private final ClaimServiceClient claimServiceClient;

    @Override
    public ReportResponse generateSummaryReport(String userRoleHeader) {
        requireAdmin(userRoleHeader);
        PolicySummaryResponse policySummary = policyServiceClient.getPolicySummary();
        ClaimSummaryResponse claimSummary = claimServiceClient.getClaimSummary();

        return ReportResponse.builder()
                .totalPolicies(policySummary.getTotalPolicies())
                .activePolicies(policySummary.getActivePolicies())
                .totalReviewedClaims(claimSummary.getApprovedClaims() + claimSummary.getRejectedClaims())
                .approvedClaims(claimSummary.getApprovedClaims())
                .rejectedClaims(claimSummary.getRejectedClaims())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public DashboardResponse generateDashboard(String userRoleHeader) {
        requireAdmin(userRoleHeader);
        PolicySummaryResponse policySummary = policyServiceClient.getPolicySummary();
        ClaimSummaryResponse claimSummary = claimServiceClient.getClaimSummary();

        return DashboardResponse.builder()
                .totalPolicies(policySummary.getTotalPolicies())
                .activePolicies(policySummary.getActivePolicies())
                .expiredPolicies(policySummary.getExpiredPolicies())
                .totalClaims(claimSummary.getTotalClaims())
                .approvedClaims(claimSummary.getApprovedClaims())
                .rejectedClaims(claimSummary.getRejectedClaims())
                .underReviewClaims(claimSummary.getUnderReviewClaims())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private void requireAdmin(String userRoleHeader) {
        if (userRoleHeader == null || userRoleHeader.isBlank()) {
            throw new UnauthorizedException("Missing user role header");
        }
        if (!ROLE_ADMIN.equalsIgnoreCase(userRoleHeader)) {
            throw new ForbiddenException("Admin role is required");
        }
    }
}
