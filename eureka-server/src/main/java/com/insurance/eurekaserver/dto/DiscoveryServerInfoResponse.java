package com.insurance.eurekaserver.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscoveryServerInfoResponse {

    private final String serviceName;
    private final String description;
    private final String status;
    private final int runningOnPort;
}
