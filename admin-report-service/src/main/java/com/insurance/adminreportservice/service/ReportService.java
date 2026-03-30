package com.insurance.adminreportservice.service;

import com.insurance.adminreportservice.dto.ReportResponse;
import com.insurance.adminreportservice.dto.DashboardResponse;

public interface ReportService {

    /**
     * Generates admin summary report.
     *
     * @param userRoleHeader authenticated role
     * @return report response
     */
    ReportResponse generateSummaryReport(String userRoleHeader);

    /**
     * Generates dashboard metrics for admins.
     *
     * @param userRoleHeader authenticated role
     * @return dashboard response
     */
    DashboardResponse generateDashboard(String userRoleHeader);
}
