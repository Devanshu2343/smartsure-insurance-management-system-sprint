package com.insurance.apigateway.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayInfo {

    private final String serviceName;
    private final String description;
    private final String status;
    private final int runningOnPort;
}
