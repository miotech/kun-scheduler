package com.miotech.kun.dataplatform;

import com.miotech.kun.dataplatform.notify.TaskAttemptStatusChangeEventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {
		"com.miotech.kun.common",
		"com.miotech.kun.security",
		"com.miotech.kun.dataplatform"})
public class DataPlatformApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DataPlatformApplication.class, args);
		TaskAttemptStatusChangeEventSubscriber taskAttemptStatusChangeEventSubscriber = context.getBean(TaskAttemptStatusChangeEventSubscriber.class);
		taskAttemptStatusChangeEventSubscriber.subscribe();
	}
}
