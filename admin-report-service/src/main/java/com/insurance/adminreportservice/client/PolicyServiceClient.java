package com.insurance.adminreportservice.client;

import com.insurance.adminreportservice.dto.PolicySummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client for Policy Service admin metrics.
 */
@FeignClient(
        name = "policy-service",
        url = "${policy-service.url}",
        configuration = com.insurance.adminreportservice.config.FeignHeaderConfig.class)
public interface PolicyServiceClient {

    @GetMapping("/api/admin/policies/summary")
    PolicySummaryResponse getPolicySummary();
}
