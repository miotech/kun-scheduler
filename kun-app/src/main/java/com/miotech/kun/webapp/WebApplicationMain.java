package com.miotech.kun.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application layer entry class for the following modules:
 * kun-data-platform
 * kun-data-dashboard
 * kun-data-discovery
 * kun-data-quality
 */
@SpringBootApplication(scanBasePackages = {
        "com.miotech.kun.common",
        "com.miotech.kun.security",
        // kun-data-platform
        "com.miotech.kun.dataplatform",
        // kun-data-dashboard
        "com.miotech.kun.datadashboard",
        // kun-data-discovery
        "com.miotech.kun.dataquality",
        "com.miotech.kun.datadiscovery"
})
public class WebApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(WebApplicationMain.class, args);
    }
}
