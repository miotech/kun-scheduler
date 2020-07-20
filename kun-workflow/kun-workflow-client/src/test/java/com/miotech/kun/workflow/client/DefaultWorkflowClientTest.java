package com.miotech.kun.workflow.client;

import com.miotech.kun.workflow.client.mock.MockKunWebServerTestBase;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskAttempt;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.client.mock.MockingFactory.mockOperator;
import static com.miotech.kun.workflow.client.mock.MockingFactory.mockTask;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class DefaultWorkflowClientTest extends MockKunWebServerTestBase {

    private DefaultWorkflowClient client;

    @Before
    public void init() {
        client = new DefaultWorkflowClient(getBaseUrl());
    }

    @Test
    public void saveOperator() {
        Operator operator = mockOperator();
        Operator result = client.saveOperator(operator.getName(), operator);
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), Matchers.is(operator.getName()));
    }

    @Test
    public void createTask_ok() {
        // prepare
        Operator operator = mockOperator();
        operator = client.saveOperator(operator.getName(), operator);

        Task task = mockTask(operator.getId());
        Task result = client.createTask(task);

        // verify
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), Matchers.is(task.getName()));

        // cleanup
        client.deleteTask(result.getId());
    }

    @Test
    public void executeTask_ok() {
        // prepare
        Operator operator = mockOperator();
        operator = client.saveOperator(operator.getName(), operator);
        Task task = mockTask(operator.getId());

        TaskRun taskRun = client.executeTask(task, null);

        // verify
        await().atMost(10, TimeUnit.SECONDS)
                .until(() ->
                runFinished(taskRun.getId()));
        TaskRun result = client.getTaskRun(taskRun.getId());
        assertTrue(result.getId() > 0);
        assertTrue(result.getStartAt() != null);
        assertTrue(result.getEndAt() != null);

        assertThat(result.getAttempts().size(), Matchers.is(1));
        TaskAttempt attempt = result.getAttempts().get(0);
        assertTrue(attempt.getStartAt() != null);
        assertTrue(attempt.getEndAt() != null);

    }

    private boolean runFinished(Long taskRunId) {
        TaskRunStatus taskRunStatus = client.getTaskRunState(taskRunId).getStatus();
        return taskRunStatus.isSuccess() || taskRunStatus.isFailure();
    }
}