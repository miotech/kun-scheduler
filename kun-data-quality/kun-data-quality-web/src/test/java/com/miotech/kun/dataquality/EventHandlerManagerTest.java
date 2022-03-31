package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.mock.MockTaskAttemptFinishedEventFactory;
import com.miotech.kun.dataquality.mock.MockTaskAttemptStatusChangeEventFactory;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetTaskAttemptEventHandler;
import com.miotech.kun.dataquality.web.service.DataQualityTaskAttemptFinishedEventHandler;
import com.miotech.kun.dataquality.web.service.EventHandlerManager;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EventHandlerManagerTest extends DataQualityTestBase {

    @Autowired
    private EventHandlerManager eventHandlerManager;

    @SpyBean
    private AbnormalDatasetTaskAttemptEventHandler abnormalDatasetTaskAttemptEventHandler;

    @SpyBean
    private DataQualityTaskAttemptFinishedEventHandler dataQualityTaskAttemptFinishedEventHandler;

    @Test
    public void testHandle_TaskAttemptFinishedEvent() {
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.SUCCESS);
        eventHandlerManager.handle(event);

        verify(abnormalDatasetTaskAttemptEventHandler, times(1)).handle(event);
        verify(dataQualityTaskAttemptFinishedEventHandler, times(1)).handle(event);
    }

    @Test
    public void testHandle_TaskAttemptStatusChangeEvent() {
        TaskAttemptStatusChangeEvent event = MockTaskAttemptStatusChangeEventFactory.create(TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS);
        eventHandlerManager.handle(event);

        verify(abnormalDatasetTaskAttemptEventHandler, times(1)).handle(event);
    }

}
