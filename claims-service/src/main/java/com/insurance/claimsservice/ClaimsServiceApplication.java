package com.insurance.claimsservice;

import com.insurance.claimsservice.config.FeignHeaderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = FeignHeaderConfig.class)
public class ClaimsServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(ClaimsServiceApplication.class, args);
    }
}
