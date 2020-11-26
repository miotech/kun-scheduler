package com.miotech.kun.workflow.client;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.client.mock.MockKunWebServerTestBase;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskAttempt;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.client.mock.MockingFactory.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DefaultWorkflowClientTest extends MockKunWebServerTestBase {

    private DefaultWorkflowClient client;

    @Before
    public void init() {
        client = new DefaultWorkflowClient(getBaseUrl());
    }

    @Test
    public void saveOperator() {
        Operator operator = mockOperator();
        client.saveOperator(operator.getName(), operator);
        Operator result = client.getOperator(operator.getName()).get();
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(operator.getName()));
        assertThat(result.getClassName(), is(operator.getClassName()));
        assertThat(result.getConfigDef().size(), is(1));
    }

    @Test
    public void updateOperatorJar() {
        Operator operator = mockOperator();
        Operator result = client.saveOperator(operator.getName(), operator);
        File jarFile =  new File(nopOperatorPath.replace("file:", ""));
        client.updateOperatorJar(result.getName(), jarFile);
        Operator updated = client.getOperator(operator.getName()).get();
        assertThat(updated.getConfigDef().size(), is(1));
        assertThat(updated.getConfigDef().get(0).getName(), is("testKey1"));
        assertThat(updated.getConfigDef().get(0).getDisplayName(), is("testKey1"));
        assertThat(updated.getConfigDef().get(0).getType(), is(ConfigDef.Type.BOOLEAN));
        assertThat(updated.getConfigDef().get(0).getDocumentation(), is("test key 1"));
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
        assertThat(result.getName(), is(task.getName()));

        // cleanup
        client.deleteTask(result.getId());
    }

    @Test
    public void updateTask_ok() {
        // prepare
        Operator operator = mockOperator();
        operator = client.saveOperator(operator.getName(), operator);

        Task task = mockTask(operator.getId());
        Task created = client.createTask(task);

        Task updated = created.cloneBuilder()
                .withName(created.getName() + "_updated")
                .withTags(Collections.singletonList(new Tag("keu", "value")))
                .build();
        Task result = client.saveTask(updated, null);
        // verify
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(updated.getName()));

        // cleanup
        client.deleteTask(result.getId());
    }

    @Test
    public void executeTask_ok() {
        // prepare
        Operator operator = mockOperator();
        operator = client.saveOperator(operator.getName(), operator);
        File jarFile =  new File(nopOperatorPath.replace("file:", ""));
        client.updateOperatorJar(operator.getName(), jarFile);

        Task task = mockTask(operator.getId());
        TaskRun taskRun = client.executeTask(task,
                ImmutableMap.of("testKey1", true));

        // verify
        await().atMost(120, TimeUnit.SECONDS)
                .until(() ->
                runFinished(taskRun.getId()));
        TaskRun result = client.getTaskRun(taskRun.getId());
        assertTrue(result.getId() > 0);
        assertTrue(result.getStartAt() != null);
        assertTrue(result.getEndAt() != null);

        assertThat(result.getAttempts().size(), is(1));
        TaskAttempt attempt = result.getAttempts().get(0);
        assertTrue(attempt.getStartAt() != null);
        assertTrue(attempt.getEndAt() != null);
    }

    @Ignore
    public void executeSparkTask() {
        // prepare
        Config config = Config.newBuilder()
                .addConfig("files","file:/Users/shiki/sparklib/shade/testLiviy-1.0-SNAPSHOT.jar")
                .addConfig("jar", Arrays.asList("file:/Users/shiki/sparklib/spark-2.4-spline-agent-bundle_2.11-0.6.0-SNAPSHOT.jar",
                        "file:/Users/shiki/sparklib/postgresql-42.1.4.jar",
                        "file:/Users/shiki/sparklib/spark-sql_2.11-2.4.2.jar"))
                .addConfig("sparkConf","{\"spline.hdfs_dispatcher.address\":\"${ spark.hdfs.address }\"}")
                .addConfig("livyHost","${ dataplatform.livy.host }")
                .addConfig("application","com.spark.sparkapp.SparkTest")
                .addConfig("sparkJobName","SparkTest")
                .build();
        Task task = Task.newBuilder()
                .withId(1L)
                .withName("SparkTest")
                .withDescription("spark lineage test")
                .withConfig(config)
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 1 * ?"))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withOperatorId(76671693556285440L)
                .build();
        TaskRun taskRun = client.executeTask(task,
                ImmutableMap.of("testKey1", true));

        // verify
        await().atMost(150, TimeUnit.SECONDS)
                .until(() ->
                        runFinished(taskRun.getId()));
        TaskRun result = client.getTaskRun(taskRun.getId());
        assertTrue(result.getId() > 0);
        assertTrue(result.getStartAt() != null);
        assertTrue(result.getEndAt() != null);

        assertThat(result.getAttempts().size(), is(1));
        TaskAttempt attempt = result.getAttempts().get(0);
        assertTrue(attempt.getStartAt() != null);
        assertTrue(attempt.getEndAt() != null);
    }

    private boolean runFinished(Long taskRunId) {
        TaskRunStatus taskRunStatus = client.getTaskRunState(taskRunId).getStatus();
        return taskRunStatus.isFinished();
    }
}