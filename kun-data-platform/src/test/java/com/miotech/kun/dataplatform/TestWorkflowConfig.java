package com.miotech.kun.dataplatform;

import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Configuration
public class TestWorkflowConfig {

    @Value("${test.images.workflow}")
    private String workflowImage;

    @Bean
    public WorkflowClient getWorkflowClient() {
        GenericContainer workflow = new GenericContainer(workflowImage)
                .withExposedPorts(8088)
                .withEnv("SERVER_PORT", "8088")
                .withEnv("APP_CONFIG_ENV", "local");
        workflow.start();
        await().atMost(30, TimeUnit.SECONDS)
        .until(workflow::isRunning);
        int port = workflow.getMappedPort(8088);

        String workflowUrl = "http://localhost:" + port;
        return new DefaultWorkflowClient(workflowUrl);
    }
}
