package com.miotech.kun.dataquality.handler;

import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataquality.DataQualityTestBase;
import com.miotech.kun.dataquality.mock.*;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.dataquality.web.persistence.AbnormalDatasetRepository;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetService;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetTaskAttemptFinishedEventHandler;
import com.miotech.kun.dataquality.web.service.MetadataClient;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class AbnormalDatasetTaskAttemptFinishedEventHandlerTest extends DataQualityTestBase {

    @Autowired
    private AbnormalDatasetTaskAttemptFinishedEventHandler abnormalDatasetTaskAttemptFinishedEventHandler;

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    private AbnormalDatasetRepository abnormalDatasetRepository;

    @SpyBean
    private MetadataClient metadataClient;

    @SpyBean
    private WorkflowClient workflowClient;

    @SpyBean
    private DeployedTaskFacade deployedTaskFacade;

    @Test
    public void testHandle_failureEvent() {
        // create taskAttemptFinishedEvent, finalStatus = TaskRunStatus.FAILED
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.FAILED);

        // prepare abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create(event.getTaskRunId());
        abnormalDatasetService.create(abnormalDataset, abnormalDataset.getDatasetGid());

        // handle event
        DatasetDetail datasetDetail = MockDatasetDetailFactory.create();
        doReturn(datasetDetail).when(metadataClient).findById(anyLong());
        Task task = MockTaskFactory.create();
        doReturn(task).when(workflowClient).getTask(anyLong());
        DeployedTask deployedTask = MockDeployedTaskFactory.create();
        doReturn(Optional.of(deployedTask)).when(deployedTaskFacade).findByWorkflowTaskId(anyLong());
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
        DatasetDetail datasetDetail = MockDatasetDetailFactory.create();
        doReturn(datasetDetail).when(metadataClient).findById(anyLong());
        Task task = MockTaskFactory.create();
        doReturn(task).when(workflowClient).getTask(anyLong());
        DeployedTask deployedTask = MockDeployedTaskFactory.create();
        doReturn(Optional.of(deployedTask)).when(deployedTaskFacade).findByWorkflowTaskId(anyLong());
        abnormalDatasetTaskAttemptFinishedEventHandler.handle(event);

        // verify
        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        AbnormalDataset abnormalDatasetOfFetch = abnormalDatasets.get(0);
        assertThat(abnormalDatasetOfFetch.getStatus(), is("SUCCESS"));
    }

    @Test
    public void testHandle_taskRunNonExistent() {
        // create taskAttemptFinishedEvent, finalStatus = TaskRunStatus.SUCCESS
        TaskAttemptFinishedEvent event = MockTaskAttemptFinishedEventFactory.create(TaskRunStatus.SUCCESS);

        // handle event
        abnormalDatasetTaskAttemptFinishedEventHandler.handle(event);

        // verify
        verify(workflowClient, never()).getTask(event.getTaskRunId());
    }

}
