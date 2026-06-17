package com.sms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Centralized Configuration Server
 * Runs on port 8888.
 * All microservices pull their shared and per-service configs from here on startup.
 *
 * In dev: reads configs from a local Git repo at ~/sms-config.
 * In Docker: override SPRING_CLOUD_CONFIG_SERVER_GIT_URI env variable.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
