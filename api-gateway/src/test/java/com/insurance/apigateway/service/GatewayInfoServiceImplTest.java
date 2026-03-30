package com.insurance.apigateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.insurance.apigateway.dto.GatewayInfoResponse;
import com.insurance.apigateway.entity.GatewayInfo;
import com.insurance.apigateway.exception.GatewayException;
import com.insurance.apigateway.repository.GatewayInfoRepository;
import com.insurance.apigateway.service.impl.GatewayInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GatewayInfoServiceImplTest {

    @Mock
    private GatewayInfoRepository gatewayInfoRepository;

    @InjectMocks
    private GatewayInfoServiceImpl gatewayInfoService;

    @Test
    void getGatewayInfoReturnsResponseWhenDataAvailable() {
        // Arrange
        GatewayInfo gatewayInfo = GatewayInfo.builder()
                .serviceName("api-gateway")
                .description("Gateway")
                .status("UP")
                .runningOnPort(8080)
                .build();

        when(gatewayInfoRepository.fetchGatewayInfo()).thenReturn(gatewayInfo);

        // Act
        GatewayInfoResponse response = gatewayInfoService.getGatewayInfo();

        // Assert
        assertEquals("api-gateway", response.getServiceName());
        assertThat(response.getDescription()).isEqualTo("Gateway");
        assertEquals("UP", response.getStatus());
        assertEquals(8080, response.getRunningOnPort());
    }

    @Test
    void getGatewayInfoThrowsWhenRepositoryReturnsNull() {
        // Arrange
        when(gatewayInfoRepository.fetchGatewayInfo()).thenReturn(null);

        // Act + Assert
        assertThrows(GatewayException.class, () -> gatewayInfoService.getGatewayInfo());
    }
}
