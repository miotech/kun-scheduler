package com.miotech.kun.workflow.worker;

import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.model.common.SpecialTick;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperatorContextImplTest extends DatabaseTestBase {

    private OperatorContext context;
    private TaskRunDao taskRunDao;
    private TaskDao taskDao;
    private Map<String, Object> config;

    @BeforeEach
    public void init(){
        config = new HashMap<>();
        config.put("args", "hello");
        context = new OperatorContextImpl(new Config(config), 1L, null, "default");
        injector.injectMembers(context);
        taskRunDao = injector.getInstance(TaskRunDao.class);
        taskDao = injector.getInstance(TaskDao.class);
    }

    @Test
    public void getScheduleTimeWithUTC_withoutScheduleTime(){
        String tick = "202110131111";
        Task taskWithUTC = MockTaskFactory.createTask().cloneBuilder().withConfig(new Config(config)).build();
        taskDao.create(taskWithUTC);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithUTC)
                .cloneBuilder()
                .withScheduledTick(new Tick(tick))
                .withScheduleTime(SpecialTick.NULL)
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue(tick.equals(context.getScheduleTime()));
    }


    @Test
    public void getScheduleTimeWithCST_withoutScheduleTime(){
        String tick = "202110131111";
        Task taskWithCST = MockTaskFactory.createTask().cloneBuilder()
                .withScheduleConf(
                        ScheduleConf.newBuilder()
                                .withType(ScheduleType.NONE)
                                .withCronExpr("0 0 20 * * ? *")
                                .withTimeZone("Asia/Shanghai")
                                .build()
                )
                .build();
        taskDao.create(taskWithCST);
        TaskRun taskRunWithCST = MockTaskRunFactory.createTaskRun(1L, taskWithCST)
                .cloneBuilder()
                .withScheduledTick(new Tick(tick))
                .withScheduleTime(SpecialTick.NULL)
                .build();
        taskRunDao.createTaskRun(taskRunWithCST);
        assertTrue("202110131911".equals(context.getScheduleTime()));
    }

    @Test
    public void getScheduleTimeUTC_withScheduleTime() {
        String tick = "202206231700";
        Task taskWithUTC = MockTaskFactory.createTask().cloneBuilder().withConfig(new Config(config)).build();
        taskDao.create(taskWithUTC);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithUTC)
                .cloneBuilder()
                .withScheduleTime(new Tick(tick))
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue(tick.equals(context.getScheduleTime()));
    }

    @Test
    public void getScheduleTimeCST_withScheduleTime() {
        String tick = "202206230900";
        Task taskWithCST = MockTaskFactory.createTask().cloneBuilder()
                .withScheduleConf(
                        ScheduleConf.newBuilder()
                                .withType(ScheduleType.NONE)
                                .withCronExpr("0 0 20 * * ? *")
                                .withTimeZone("Asia/Shanghai")
                                .build()
                )
                .build();
        taskDao.create(taskWithCST);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithCST)
                .cloneBuilder()
                .withScheduleTime(new Tick(tick))
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue("202206231700".equals(context.getScheduleTime()));
    }

    @Test
    public void getScheduleTimeUTC_withEmptyScheduleTime() {
        String scheduledTick = "202206231000";
        Task taskWithUTC = MockTaskFactory.createTask().cloneBuilder().withConfig(new Config(config)).build();
        taskDao.create(taskWithUTC);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithUTC)
                .cloneBuilder()
                .withScheduledTick(new Tick(scheduledTick))
                .withScheduleTime(SpecialTick.NULL)
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue(scheduledTick.equals(context.getScheduleTime()));
    }

    @Test
    public void getQueueName() {
        String queueName = context.getQueueName();
        assertTrue(queueName.equals("default"));
    }
}