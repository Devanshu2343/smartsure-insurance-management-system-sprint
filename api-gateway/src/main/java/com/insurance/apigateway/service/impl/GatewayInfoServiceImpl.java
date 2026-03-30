package com.insurance.apigateway.service.impl;

import com.insurance.apigateway.dto.GatewayInfoResponse;
import com.insurance.apigateway.entity.GatewayInfo;
import com.insurance.apigateway.exception.GatewayException;
import com.insurance.apigateway.repository.GatewayInfoRepository;
import com.insurance.apigateway.service.GatewayInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayInfoServiceImpl implements GatewayInfoService {

    private final GatewayInfoRepository gatewayInfoRepository;

    @Override
    public GatewayInfoResponse getGatewayInfo() {
        GatewayInfo gatewayInfo = gatewayInfoRepository.fetchGatewayInfo();
        if (gatewayInfo == null) {
            throw new GatewayException("Unable to load gateway information");
        }

        log.info("Gateway info loaded for service: {}", gatewayInfo.getServiceName());

        return GatewayInfoResponse.builder()
                .serviceName(gatewayInfo.getServiceName())
                .description(gatewayInfo.getDescription())
                .status(gatewayInfo.getStatus())
                .runningOnPort(gatewayInfo.getRunningOnPort())
                .build();
    }
}
