package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.mock.MockDatasetDetailFactory;
import com.miotech.kun.dataquality.mock.MockExpectationFactory;
import com.miotech.kun.dataquality.mock.MockTaskAttemptFinishedEventFactory;
import com.miotech.kun.dataquality.mock.MockValidationResultFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.service.ExpectationFailureTaskAttemptFinishedEventHandler;
import com.miotech.kun.dataquality.web.service.MetadataClient;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class ExpectationFailureTaskAttemptFinishedEventHandlerTest extends DataQualityTestBase {

    @Autowired
    ExpectationFailureTaskAttemptFinishedEventHandler expectationFailureTaskAttemptFinishedEventHandler;

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @SpyBean
    private NotifyFacade notifyFacade;

    @SpyBean
    private MetadataClient metadataClient;

    @Test
    public void testHandler_failed() {
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.FAILED);

        Expectation expectation = MockExpectationFactory.createWithTaskId(event.getTaskId());
        expectationDao.create(expectation);

        DatasetDetail datasetDetail = MockDatasetDetailFactory.create();
        doReturn(datasetDetail).when(metadataClient).findById(expectation.getDataset().getGid());

        ValidationResult validationResult = MockValidationResultFactory.create(expectation.getExpectationId(), false);
        expectationRunDao.create(validationResult);
        expectationFailureTaskAttemptFinishedEventHandler.handle(event);

        verify(notifyFacade, times(1)).notify(anyList(), anyString());

    }

    @Test
    public void testHandler_success() {
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.SUCCESS);

        Expectation expectation = MockExpectationFactory.createWithTaskId(event.getTaskId());
        expectationDao.create(expectation);

        ValidationResult validationResult = MockValidationResultFactory.create(expectation.getExpectationId(), true);
        expectationRunDao.create(validationResult);
        expectationFailureTaskAttemptFinishedEventHandler.handle(event);

        verify(notifyFacade, times(0)).notify(anyList(), anyString());

    }

}
