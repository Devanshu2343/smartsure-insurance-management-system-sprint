package com.insurance.adminreportservice;

import com.insurance.adminreportservice.config.FeignHeaderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = FeignHeaderConfig.class)
public class AdminReportServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(AdminReportServiceApplication.class, args);
    }
}
