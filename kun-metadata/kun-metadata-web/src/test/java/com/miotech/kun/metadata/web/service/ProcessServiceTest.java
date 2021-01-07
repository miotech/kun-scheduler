package com.miotech.kun.metadata.web.service;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

public class ProcessServiceTest {

    private ProcessService processService = new ProcessService();

    private WorkflowClient workflowClient;
    private Props props;
    private Random random;

    @Before
    public void setUp() {
        this.workflowClient = Mockito.mock(WorkflowClient.class);
        this.props = initProperties();
        this.random = new Random();

        Reflect.on(processService).set("workflowClient", workflowClient);
        Reflect.on(processService).set("props", props);
    }

    private Props initProperties() {
        Props props = new Props();
        props.put(TaskParam.MCE_TASK.getName(), String.valueOf(System.currentTimeMillis()));
        return props;
    }

    @Test
    public void testSubmit() {
        Long taskRunId = random.nextLong();

        Mockito.when(workflowClient.executeTask(eq(props.getLong(TaskParam.MCE_TASK.getName())), anyMap()))
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
