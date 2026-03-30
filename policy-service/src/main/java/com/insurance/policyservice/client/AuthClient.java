package com.insurance.policyservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "auth-service",
        url = "${auth-service.url}")
public interface AuthClient {

    @GetMapping("/api/auth/validate")
    Boolean validateUser(@RequestParam("email") String email);
}
