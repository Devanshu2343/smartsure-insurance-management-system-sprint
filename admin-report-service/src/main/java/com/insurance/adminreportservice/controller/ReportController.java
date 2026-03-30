package com.insurance.adminreportservice.controller;

import com.insurance.adminreportservice.dto.DashboardResponse;
import com.insurance.adminreportservice.dto.ReportResponse;
import com.insurance.adminreportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin reporting endpoints.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/reports")
    public ResponseEntity<ReportResponse> generateSummaryReport(
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(reportService.generateSummaryReport(userRole));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> generateDashboard(
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(reportService.generateDashboard(userRole));
    }
}
