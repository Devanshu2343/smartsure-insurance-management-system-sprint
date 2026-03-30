package com.insurance.apigateway.controller;

import com.insurance.apigateway.dto.GatewayInfoResponse;
import com.insurance.apigateway.service.GatewayInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class GatewayInfoController {

    private final GatewayInfoService gatewayInfoService;

    @GetMapping("/info")
    public ResponseEntity<GatewayInfoResponse> getGatewayInfo() {
        return ResponseEntity.ok(gatewayInfoService.getGatewayInfo());
    }
}
