package com.miotech.kun.datadashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: Jie Chen
 * @created: 2020/9/16
 */
@SpringBootApplication(scanBasePackages = {"com.miotech.kun.datadashboard",
        "com.miotech.kun.common",
        "com.miotech.kun.security"})
public class DataDashboardServer {

    public static void main(String[] args) {
        SpringApplication.run(DataDashboardServer.class, args);
    }
}
