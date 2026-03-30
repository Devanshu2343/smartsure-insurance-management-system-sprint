package com.insurance.adminreportservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.insurance.adminreportservice.client.ClaimServiceClient;
import com.insurance.adminreportservice.client.PolicyServiceClient;
import com.insurance.adminreportservice.dto.ClaimSummaryResponse;
import com.insurance.adminreportservice.dto.DashboardResponse;
import com.insurance.adminreportservice.dto.PolicySummaryResponse;
import com.insurance.adminreportservice.dto.ReportResponse;
import com.insurance.adminreportservice.exception.ForbiddenException;
import com.insurance.adminreportservice.exception.UnauthorizedException;
import com.insurance.adminreportservice.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private PolicyServiceClient policyServiceClient;

    @Mock
    private ClaimServiceClient claimServiceClient;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void generateSummaryReportAggregatesCounts() {
        // Arrange
        PolicySummaryResponse policySummary = mock(PolicySummaryResponse.class);
        when(policySummary.getTotalPolicies()).thenReturn(10L);
        when(policySummary.getActivePolicies()).thenReturn(7L);

        ClaimSummaryResponse claimSummary = mock(ClaimSummaryResponse.class);
        when(claimSummary.getApprovedClaims()).thenReturn(3L);
        when(claimSummary.getRejectedClaims()).thenReturn(1L);

        when(policyServiceClient.getPolicySummary()).thenReturn(policySummary);
        when(claimServiceClient.getClaimSummary()).thenReturn(claimSummary);

        // Act
        ReportResponse response = reportService.generateSummaryReport("ADMIN");

        // Assert
        assertEquals(10L, response.getTotalPolicies());
        assertEquals(7L, response.getActivePolicies());
        assertEquals(4L, response.getTotalReviewedClaims());
    }

    @Test
    void generateDashboardAggregatesRemoteSummaries() {
        // Arrange
        PolicySummaryResponse policySummary = mock(PolicySummaryResponse.class);
        when(policySummary.getTotalPolicies()).thenReturn(5L);
        when(policySummary.getActivePolicies()).thenReturn(2L);
        when(policySummary.getExpiredPolicies()).thenReturn(1L);

        ClaimSummaryResponse claimSummary = mock(ClaimSummaryResponse.class);
        when(claimSummary.getTotalClaims()).thenReturn(8L);
        when(claimSummary.getApprovedClaims()).thenReturn(3L);
        when(claimSummary.getRejectedClaims()).thenReturn(1L);
        when(claimSummary.getUnderReviewClaims()).thenReturn(2L);

        when(policyServiceClient.getPolicySummary()).thenReturn(policySummary);
        when(claimServiceClient.getClaimSummary()).thenReturn(claimSummary);

        // Act
        DashboardResponse response = reportService.generateDashboard("ADMIN");

        // Assert
        assertEquals(5L, response.getTotalPolicies());
        assertEquals(2L, response.getActivePolicies());
        assertEquals(1L, response.getExpiredPolicies());
        assertEquals(8L, response.getTotalClaims());
        assertThat(response.getGeneratedAt()).isNotNull();
    }

    @Test
    void generateSummaryReportThrowsWhenRoleMissing() {
        // Act + Assert
        assertThrows(UnauthorizedException.class, () -> reportService.generateSummaryReport(null));
    }

    @Test
    void generateDashboardThrowsWhenRoleNotAdmin() {
        // Act + Assert
        assertThrows(ForbiddenException.class, () -> reportService.generateDashboard("CUSTOMER"));
    }
}
