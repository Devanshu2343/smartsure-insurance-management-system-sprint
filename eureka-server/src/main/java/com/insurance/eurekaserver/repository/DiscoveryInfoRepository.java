package com.insurance.eurekaserver.repository;

import com.insurance.eurekaserver.entity.DiscoveryServerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class DiscoveryInfoRepository {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private int serverPort;

    public DiscoveryServerInfo fetchServerInfo() {
        return DiscoveryServerInfo.builder()
                .serviceName(serviceName)
                .description("Central service registry for all insurance microservices")
                .status("UP")
                .runningOnPort(serverPort)
                .build();
    }
}
