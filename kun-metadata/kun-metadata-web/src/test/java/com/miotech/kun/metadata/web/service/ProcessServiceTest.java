package com.miotech.kun.metadata.web.service;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.model.vo.PullProcessVO;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

public class ProcessServiceTest {

    private ProcessService processService = new ProcessService();

    private WorkflowServiceFacade workflowServiceFacade;
    private Props props;
    private Random random;

    @Before
    public void setUp() {
        this.workflowServiceFacade = Mockito.mock(WorkflowServiceFacade.class);
        this.props = initProperties();
        this.random = new Random();

        Reflect.on(processService).set("workflowServiceFacade", workflowServiceFacade);
        Reflect.on(processService).set("props", props);
    }

    private Props initProperties() {
        Props props = new Props();
        props.put(TaskParam.MCE_TASK.getName(), String.valueOf(System.currentTimeMillis()));
        return props;
    }

    // TODO: enable this test
    @Test
    @Ignore
    public void testSubmit() {
        Long taskRunId = random.nextLong();
        Task task = MockTaskFactory.createTask();

        Mockito.when(workflowServiceFacade.executeTask(eq(props.getLong(TaskParam.MCE_TASK.getName())), anyMap()))
                .thenReturn(MockTaskRunFactory.createTaskRun(taskRunId, task));
        PullProcessVO pullProcessVO = processService.submitPull(taskRunId, DataBuilderDeployMode.DATASOURCE);

        assertThat(pullProcessVO.getLatestMCETaskRun().getId(), is(taskRunId.toString()));
    }

    @Test
    public void testFetchStatus() {
        Long id = random.nextLong();
        Task task = MockTaskFactory.createTask();
        Mockito.when(workflowServiceFacade.getTaskRun(id)).thenReturn(MockTaskRunFactory.createTaskRun(id, task));
        TaskRun taskRun = processService.fetchStatus(id.toString());

        assertThat(taskRun.getId(), is(id));
    }

}
