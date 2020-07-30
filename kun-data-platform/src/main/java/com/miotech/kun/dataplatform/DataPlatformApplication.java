package com.miotech.kun.dataplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.miotech.kun.common",
		"com.miotech.kun.security",
		"com.miotech.kun.dataplatform"})
public class DataPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataPlatformApplication.class, args);
	}
}
