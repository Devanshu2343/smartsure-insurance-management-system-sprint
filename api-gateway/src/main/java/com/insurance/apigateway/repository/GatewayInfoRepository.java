package com.insurance.apigateway.repository;

import com.insurance.apigateway.entity.GatewayInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class GatewayInfoRepository {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private int serverPort;

    public GatewayInfo fetchGatewayInfo() {
        return GatewayInfo.builder()
                .serviceName(serviceName)
                .description("Central gateway for Insurance Management microservices")
                .status("UP")
                .runningOnPort(serverPort)
                .build();
    }
}
