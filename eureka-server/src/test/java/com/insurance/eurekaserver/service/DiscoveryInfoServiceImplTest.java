package com.insurance.eurekaserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.insurance.eurekaserver.dto.DiscoveryServerInfoResponse;
import com.insurance.eurekaserver.entity.DiscoveryServerInfo;
import com.insurance.eurekaserver.exception.DiscoveryServerException;
import com.insurance.eurekaserver.repository.DiscoveryInfoRepository;
import com.insurance.eurekaserver.service.impl.DiscoveryInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscoveryInfoServiceImplTest {

    @Mock
    private DiscoveryInfoRepository discoveryInfoRepository;

    @InjectMocks
    private DiscoveryInfoServiceImpl discoveryInfoService;

    @Test
    void getServerInfoReturnsResponseWhenDataAvailable() {
        // Arrange
        DiscoveryServerInfo info = DiscoveryServerInfo.builder()
                .serviceName("eureka-server")
                .description("Discovery")
                .status("UP")
                .runningOnPort(8761)
                .build();

        when(discoveryInfoRepository.fetchServerInfo()).thenReturn(info);

        // Act
        DiscoveryServerInfoResponse response = discoveryInfoService.getServerInfo();

        // Assert
        assertEquals("eureka-server", response.getServiceName());
        assertThat(response.getDescription()).isEqualTo("Discovery");
        assertEquals("UP", response.getStatus());
        assertEquals(8761, response.getRunningOnPort());
    }

    @Test
    void getServerInfoThrowsWhenRepositoryReturnsNull() {
        // Arrange
        when(discoveryInfoRepository.fetchServerInfo()).thenReturn(null);

        // Act + Assert
        assertThrows(DiscoveryServerException.class, () -> discoveryInfoService.getServerInfo());
    }
}
