package com.miotech.kun.metadata.web.service;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

public class MSEServiceTest {

    private MSEService mseService;

    @Test
    public void testExecute() {
        // mock
        Props props = Mockito.mock(Props.class);
        Mockito.when(props.getLong(TaskParam.MSE_TASK.getName())).thenReturn(randomLong());

        WorkflowServiceFacade workflowServiceFacade = Mockito.mock(WorkflowServiceFacade.class);
        Mockito.when(workflowServiceFacade.executeTask(eq(props.getLong(TaskParam.MSE_TASK.getName())), anyMap()))
                .thenReturn(MockTaskRunFactory.createTaskRun(MockTaskFactory.createTask()));

        mseService = new MSEService(props, workflowServiceFacade);

        // execute
        MetadataStatisticsEvent mse = new MetadataStatisticsEvent(MetadataStatisticsEvent.EventType.FIELD, randomLong(), randomLong());
        TaskRun taskRun = mseService.execute(mse);

        // assert
        assertThat(taskRun, notNullValue());
        assertThat(taskRun.getTask(), notNullValue());
        assertThat(taskRun.getStatus(), is(TaskRunStatus.CREATED));
    }

    private long randomLong() {
        return new Random().nextLong();
    }

}
