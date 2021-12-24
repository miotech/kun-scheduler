package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.mock.MockAbnormalDatasetFactory;
import com.miotech.kun.dataquality.mock.MockTaskAttemptFinishedEventFactory;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.dataquality.web.persistence.AbnormalDatasetRepository;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetService;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetTaskAttemptFinishedEventHandler;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AbnormalDatasetTaskAttemptFinishedEventHandlerTest extends DataQualityTestBase {

    @Autowired
    private AbnormalDatasetTaskAttemptFinishedEventHandler abnormalDatasetTaskAttemptFinishedEventHandler;

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    private AbnormalDatasetRepository abnormalDatasetRepository;

    @Test
    public void testHandle_failureEvent() {
        // create taskAttemptFinishedEvent, finalStatus = TaskRunStatus.FAILED
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.FAILED);

        // prepare abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create(event.getTaskRunId());
        abnormalDatasetService.create(abnormalDataset, abnormalDataset.getDatasetGid());

        // handle event
        abnormalDatasetTaskAttemptFinishedEventHandler.handle(event);

        // verify
        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        AbnormalDataset abnormalDatasetOfFetch = abnormalDatasets.get(0);
        assertThat(abnormalDatasetOfFetch.getStatus(), is("FAILED"));
    }

    @Test
    public void testHandle_successEvent() {
        // create taskAttemptFinishedEvent, finalStatus = TaskRunStatus.SUCCESS
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.SUCCESS);

        // prepare abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create(event.getTaskRunId());
        abnormalDatasetService.create(abnormalDataset, abnormalDataset.getDatasetGid());

        // handle event
        abnormalDatasetTaskAttemptFinishedEventHandler.handle(event);

        // verify
        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        AbnormalDataset abnormalDatasetOfFetch = abnormalDatasets.get(0);
        assertThat(abnormalDatasetOfFetch.getStatus(), is("SUCCESS"));
    }

}
