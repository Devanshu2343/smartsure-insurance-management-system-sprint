package com.insurance.claimsservice.client;

import com.insurance.claimsservice.config.FeignHeaderConfig;
import com.insurance.claimsservice.dto.PolicyInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Policy Service lookups.
 */
@FeignClient(
        name = "policy-service",
        url = "${policy-service.url}",
        configuration = FeignHeaderConfig.class)
public interface PolicyClient {

    @GetMapping("/api/policies/{id}")
    PolicyInfoResponse getPolicyById(@PathVariable("id") Long policyId);
}
