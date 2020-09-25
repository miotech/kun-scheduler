package com.miotech.kun.datadiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: Melo
 * @created: 5/26/20
 */
@SpringBootApplication(scanBasePackages = {"com.miotech.kun.datadiscovery",
        "com.miotech.kun.dataquality",
        "com.miotech.kun.common",
        "com.miotech.kun.security"})
public class DataDiscoveryServer {

    public static void main(String[] args) {
        SpringApplication.run(DataDiscoveryServer.class, args);
    }
}
