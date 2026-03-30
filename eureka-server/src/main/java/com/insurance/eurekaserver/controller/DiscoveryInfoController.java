package com.insurance.eurekaserver.controller;

import com.insurance.eurekaserver.dto.DiscoveryServerInfoResponse;
import com.insurance.eurekaserver.service.DiscoveryInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
public class DiscoveryInfoController {

    private final DiscoveryInfoService discoveryInfoService;

    @GetMapping("/info")
    public ResponseEntity<DiscoveryServerInfoResponse> getServerInfo() {
        return ResponseEntity.ok(discoveryInfoService.getServerInfo());
    }
}
