package com.sms.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Request Service — placeholder for Day 2 implementation.
 * Runs on port 8083.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RequestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}
