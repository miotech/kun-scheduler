package com.miotech.kun.dataplatform;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.miotech.kun.common",
		"com.miotech.kun.security",
		"com.miotech.kun.dataplatform"})
@EnableDubbo(scanBasePackages = "com.miotech.kun.security.facade")
public class DataPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataPlatformApplication.class, args);
	}
}
