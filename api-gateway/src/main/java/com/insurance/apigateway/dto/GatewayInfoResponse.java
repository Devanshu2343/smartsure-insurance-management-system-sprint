package com.insurance.apigateway.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayInfoResponse {

    private final String serviceName;
    private final String description;
    private final String status;
    private final int runningOnPort;
}
