package com.miotech.kun.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.miotech.kun.common",
        "com.miotech.kun.security",
        "com.miotech.kun.dataplatform"})
public class WebApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(WebApplicationMain.class, args);
    }
}
