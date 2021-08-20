package com.miotech.kun.workflow.client;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.client.mock.MockKunWebServerTestBase;
import com.miotech.kun.workflow.client.model.*;
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
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.client.mock.MockingFactory.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
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

        File jarFile =  new File(nopOperatorPath.replace("file:", ""));
        client.updateOperatorJar(operator.getName(), jarFile);

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

        TaskRun taskRun2 = client.executeTask(task,
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
        assertTrue(taskRun.getTask().getId().equals(taskRun2.getTask().getId()));
    }

    @Test
    public void executeTasks(){
        // prepare
        Operator operator = mockOperator();
        operator = client.saveOperator(operator.getName(), operator);
        File jarFile =  new File(nopOperatorPath.replace("file:", ""));
        client.updateOperatorJar(operator.getName(), jarFile);
        RunTaskRequest runTaskRequest = new RunTaskRequest();
        Config config = Config.newBuilder().addConfig("testKey1",false).build();
        Task task1 =  mockTask(operator.getId()).cloneBuilder().withConfig(config).build();
        Task created1 = client.createTask(task1);
        runTaskRequest.addTaskConfig(created1.getId(),new HashMap<>());
        Task task2 = task1.cloneBuilder().withName("executeTask2").build();
        Task created2 = client.createTask(task2);
        runTaskRequest.addTaskConfig(created2.getId(),new HashMap<>());

        //execute
        Map<Long, TaskRun> taskRunMap = client.executeTasks(runTaskRequest);
        assertThat(taskRunMap,hasKey(created1.getId()));
        assertThat(taskRunMap,hasKey(created2.getId()));
        assertThat(taskRunMap.get(created1.getId()).getId(),greaterThan(0l));
        assertThat(taskRunMap.get(created2.getId()).getId(),greaterThan(0l));

        // cleanup
        client.deleteTask(created1.getId());
        client.deleteTask(created2.getId());
    }

    @Ignore
    //todo:删除task需要把对应的taskRun、taskAttempt清除
    public void searchTaskRunsWithScheduleType(){
        // prepare
        // prepare
        Operator operator = mockOperator();
        operator = client.saveOperator(operator.getName(), operator);
        File jarFile =  new File(nopOperatorPath.replace("file:", ""));
        client.updateOperatorJar(operator.getName(), jarFile);
        RunTaskRequest runTaskRequest = new RunTaskRequest();
        Config config = Config.newBuilder().addConfig("testKey1",false).build();
        Task task1 =  mockTask(operator.getId()).cloneBuilder()
                .withConfig(config)
                .withScheduleConf(ScheduleConf
                        .newBuilder()
                        .withType(ScheduleType.SCHEDULED)
                        .withCronExpr("0 0 0 * * ?")
                        .build())
                .build();
        Task created1 = client.createTask(task1);
        runTaskRequest.addTaskConfig(created1.getId(),new HashMap<>());
        Task task2 = task1.cloneBuilder().withName("test2")
                .withConfig(Config.EMPTY)
                .withScheduleConf(ScheduleConf.newBuilder()
                        .withType(ScheduleType.NONE)
                        .build()
                ).build();
        Task created2 = client.createTask(task2);
        runTaskRequest.addTaskConfig(created2.getId(),new HashMap<>());

        //execute
        Map<Long, TaskRun> taskRunMap = client.executeTasks(runTaskRequest);
        TaskRun taskRun1 = taskRunMap.get(created1.getId());
        TaskRun taskRun2 = taskRunMap.get(created2.getId());
        assertThat(taskRunMap,hasKey(created1.getId()));
        assertThat(taskRunMap,hasKey(created2.getId()));
        assertThat(taskRun1.getId(),greaterThan(0l));
        assertThat(taskRun2.getId(),greaterThan(0l));

        //search
        TaskRunSearchRequest request = TaskRunSearchRequest
                .newBuilder()
                .withPageNum(0)
                .withPageSize(10)
                .withScheduleTypes(Arrays.asList(ScheduleType.NONE.name()))
                .build();
        PaginationResult<TaskRun> taskRuns = client.searchTaskRun(request);
        assertThat(taskRuns.getPageNum(),is(0));
        assertThat(taskRuns.getPageSize(),is(10));
        assertThat(taskRuns.getRecords(),hasSize(2));
        assertThat(taskRuns.getTotalCount(),is(2));

        TaskRunSearchRequest request2 = TaskRunSearchRequest
                .newBuilder()
                .withPageNum(0)
                .withPageSize(10)
                .withScheduleTypes(Arrays.asList(ScheduleType.SCHEDULED.name()))
                .build();
        PaginationResult<TaskRun> taskRuns2 = client.searchTaskRun(request2);
        assertThat(taskRuns2.getPageNum(),is(0));
        assertThat(taskRuns2.getPageSize(),is(10));
        assertThat(taskRuns2.getRecords(),hasSize(0));
        assertThat(taskRuns2.getTotalCount(),is(0));

        // cleanup
        client.deleteTask(created1.getId());
        client.deleteTask(created2.getId());

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
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 1 * ?", ZoneOffset.UTC.getId()))
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