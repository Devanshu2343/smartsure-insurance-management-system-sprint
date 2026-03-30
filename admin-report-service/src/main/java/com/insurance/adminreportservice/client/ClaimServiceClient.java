package com.insurance.adminreportservice.client;

import com.insurance.adminreportservice.dto.ClaimSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client for Claim Service admin metrics.
 */
@FeignClient(
        name = "claims-service",
        url = "${claims-service.url}",
        configuration = com.insurance.adminreportservice.config.FeignHeaderConfig.class)
public interface ClaimServiceClient {

    @GetMapping("/api/admin/claims/summary")
    ClaimSummaryResponse getClaimSummary();
}
