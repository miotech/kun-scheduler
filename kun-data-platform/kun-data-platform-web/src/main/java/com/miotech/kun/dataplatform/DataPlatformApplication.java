package com.miotech.kun.dataplatform;

import com.miotech.kun.dataplatform.notify.TaskAttemptStatusChangeEventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

@EnableDubbo(scanBasePackages = "com.miotech.kun.metadata.web.rpc")
@SpringBootApplication(scanBasePackages = {
		"com.miotech.kun.common",
		"com.miotech.kun.security",
		"com.miotech.kun.dataplatform"})
public class DataPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataPlatformApplication.class, args);
	}
}
