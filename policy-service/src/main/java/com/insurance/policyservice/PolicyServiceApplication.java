package com.insurance.policyservice;

import com.insurance.policyservice.config.FeignTracingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = FeignTracingConfig.class)
public class PolicyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyServiceApplication.class, args);
    }
}
