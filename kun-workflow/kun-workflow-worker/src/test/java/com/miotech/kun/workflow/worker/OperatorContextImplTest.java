package com.miotech.kun.workflow.worker;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OperatorContextImplTest extends DatabaseTestBase {

    private OperatorContext context;
    private TaskRunDao taskRunDao;
    private TaskDao taskDao;
    private Map<String, Object> config;

    @Before
    public void init(){
        config = new HashMap<>();
        config.put("args", "hello");
        context = new OperatorContextImpl(new Config(config), 1L);
        injector.injectMembers(context);
        taskRunDao = injector.getInstance(TaskRunDao.class);
        taskDao = injector.getInstance(TaskDao.class);
    }

    @Test
    public void getExecuteTimeWithUTC(){
        String tick = "202110131111";
        Task taskWithUTC = MockTaskFactory.createTask().cloneBuilder().withConfig(new Config(config)).build();
        taskDao.create(taskWithUTC);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithUTC)
                .cloneBuilder()
                .withScheduledTick(new Tick(tick))
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue(tick.equals(context.getExecuteTime()));
    }


    @Test
    public void getExecuteTimeWithCST(){
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
                .build();
        taskRunDao.createTaskRun(taskRunWithCST);
        assertTrue("202110131911".equals(context.getExecuteTime()));
    }
}