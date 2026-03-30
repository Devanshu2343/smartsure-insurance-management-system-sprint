package com.insurance.eurekaserver.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscoveryServerInfo {

    private final String serviceName;
    private final String description;
    private final String status;
    private final int runningOnPort;
}
