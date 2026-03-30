package com.insurance.eurekaserver.service.impl;

import com.insurance.eurekaserver.dto.DiscoveryServerInfoResponse;
import com.insurance.eurekaserver.entity.DiscoveryServerInfo;
import com.insurance.eurekaserver.exception.DiscoveryServerException;
import com.insurance.eurekaserver.repository.DiscoveryInfoRepository;
import com.insurance.eurekaserver.service.DiscoveryInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryInfoServiceImpl implements DiscoveryInfoService {

    private final DiscoveryInfoRepository discoveryInfoRepository;

    @Override
    public DiscoveryServerInfoResponse getServerInfo() {
        DiscoveryServerInfo info = discoveryInfoRepository.fetchServerInfo();
        if (info == null) {
            throw new DiscoveryServerException("Unable to load Eureka server info");
        }

        log.info("Loaded discovery server info for service: {}", info.getServiceName());

        return DiscoveryServerInfoResponse.builder()
                .serviceName(info.getServiceName())
                .description(info.getDescription())
                .status(info.getStatus())
                .runningOnPort(info.getRunningOnPort())
                .build();
    }
}
