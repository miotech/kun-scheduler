package com.miotech.kun.datadiscover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: Melo
 * @created: 5/26/20
 */
@SpringBootApplication(scanBasePackages = {"com.miotech.kun.datadiscover",
		"com.miotech.kun.common",
		"com.miotech.kun.security"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
