package com.sms.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Auth Service — handles user registration, login, and JWT issuance.
 * Runs on port 8081.
 * Registers with Eureka for service discovery.
 * JPA auditing enabled for automatic createdAt/updatedAt fields.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
