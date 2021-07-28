package com.miotech.kun.webapp;

import com.miotech.kun.webapp.listener.KunAppPropertySourceListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

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
        "com.miotech.kun.datadiscovery",
        "com.miotech.kun.webapp"
})
public class WebApplicationMain {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WebApplicationMain.class)
                .listeners(new KunAppPropertySourceListener())
                .run(args);
    }
}
