package com.miotech.kun.datadiscovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * @author: Melo
 * @created: 5/26/20
 */
@SpringBootApplication(scanBasePackages = {"com.miotech.kun.datadiscovery",
        "com.miotech.kun.dataquality",
        "com.miotech.kun.common",
        "com.miotech.kun.security"})
public class Application {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
