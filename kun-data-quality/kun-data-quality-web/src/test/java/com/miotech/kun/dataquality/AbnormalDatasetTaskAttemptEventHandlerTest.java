package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.mock.MockAbnormalDatasetFactory;
import com.miotech.kun.dataquality.mock.MockTaskAttemptFinishedEventFactory;
import com.miotech.kun.dataquality.mock.MockTaskAttemptStatusChangeEventFactory;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.dataquality.web.persistence.AbnormalDatasetRepository;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetService;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetTaskAttemptEventHandler;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AbnormalDatasetTaskAttemptEventHandlerTest extends DataQualityTestBase {

    @Autowired
    private AbnormalDatasetTaskAttemptEventHandler abnormalDatasetTaskAttemptEventHandler;

    @SpyBean
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    private AbnormalDatasetRepository abnormalDatasetRepository;

    @Test
    public void testHandle_TaskAttemptFinishedEvent_failureEvent() {
        // create taskAttemptFinishedEvent, finalStatus = TaskRunStatus.FAILED
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.FAILED);

        // prepare abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create(event.getTaskRunId());
        abnormalDatasetService.create(abnormalDataset, abnormalDataset.getDatasetGid());

        // handle event
        abnormalDatasetTaskAttemptEventHandler.handle(event);

        // verify
        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        AbnormalDataset abnormalDatasetOfFetch = abnormalDatasets.get(0);
        assertThat(abnormalDatasetOfFetch.getStatus(), is("FAILED"));
    }

    @Test
    public void testHandle_TaskAttemptFinishedEvent_successEvent() {
        // create taskAttemptFinishedEvent, finalStatus = TaskRunStatus.SUCCESS
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.SUCCESS);

        // prepare abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create(event.getTaskRunId());
        abnormalDatasetService.create(abnormalDataset, abnormalDataset.getDatasetGid());

        // handle event
        abnormalDatasetTaskAttemptEventHandler.handle(event);

        // verify
        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        AbnormalDataset abnormalDatasetOfFetch = abnormalDatasets.get(0);
        assertThat(abnormalDatasetOfFetch.getStatus(), is("SUCCESS"));
    }

    @Test
    public void testHandle_TaskAttemptStatusChangeEvent_toSuccessStatus() {
        TaskAttemptStatusChangeEvent event = MockTaskAttemptStatusChangeEventFactory.create(TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS);
        abnormalDatasetTaskAttemptEventHandler.handle(event);
        verify(abnormalDatasetService, times(0)).updateStatusByTaskRunId(anyLong(), anyString());
    }

    @Test
    public void testHandle_TaskAttemptStatusChangeEvent_toUpstreamFailedStatus() {
        TaskAttemptStatusChangeEvent event = MockTaskAttemptStatusChangeEventFactory.create(TaskRunStatus.RUNNING, TaskRunStatus.UPSTREAM_FAILED);
        abnormalDatasetTaskAttemptEventHandler.handle(event);
        verify(abnormalDatasetService, times(1)).updateStatusByTaskRunId(anyLong(), anyString());
    }

}
