package com.miotech.kun.metadata.web.service;

import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Properties;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

public class ProcessServiceTest {

    private ProcessService processService = new ProcessService();

    private WorkflowClient workflowClient;
    private Properties properties;
    private Random random;

    @Before
    public void setUp() {
        this.workflowClient = Mockito.mock(WorkflowClient.class);
        this.properties = initProperties();
        this.random = new Random();

        Reflect.on(processService).set("workflowClient", workflowClient);
        Reflect.on(processService).set("properties", properties);
    }

    private Properties initProperties() {
        Properties properties = new Properties();
        properties.setProperty(TaskParam.REFRESH.getTaskKey(), String.valueOf(System.currentTimeMillis()));
        return properties;
    }

    @Test
    public void testSubmit() {
        Long taskRunId = random.nextLong();

        Mockito.when(workflowClient.executeTask(eq(Long.parseLong(properties.getProperty(TaskParam.REFRESH.getTaskKey()))), anyMap()))
                .thenReturn(TaskRun.newBuilder().withId(taskRunId).build());
        String processId = processService.submit(taskRunId, DataBuilderDeployMode.DATASOURCE);

        assertThat(processId, is(taskRunId.toString()));
    }

    @Test
    public void testFetchStatus() {
        Long id = random.nextLong();

        Mockito.when(workflowClient.getTaskRun(id)).thenReturn(TaskRun.newBuilder().withId(id).build());
        TaskRun taskRun = processService.fetchStatus(id.toString());

        assertThat(taskRun.getId(), is(id));
    }

}
