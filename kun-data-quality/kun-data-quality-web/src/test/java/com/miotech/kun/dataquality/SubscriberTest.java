package com.miotech.kun.dataquality;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.event.PrivateEvent;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.mock.MockExpectationRequestFactory;
import com.miotech.kun.dataquality.mock.MockExpectationFactory;
import com.miotech.kun.dataquality.mock.MockOperatorFactory;
import com.miotech.kun.dataquality.mock.MockSubscriber;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.bo.ExpectationRequest;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.service.*;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetInfo;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import com.miotech.kun.dataquality.core.hooks.DataQualityCheckHook;
import com.miotech.kun.dataquality.core.model.DataQualityContext;
import com.miotech.kun.dataquality.core.expectation.CaseType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class SubscriberTest extends DataQualityTestBase {

    @Autowired
    private MockSubscriber eventSubscriber;

    @Autowired
    private DataQualityService dataQualityService;

    @Autowired
    private DataQualityRepository dataQualityRepository;

    @Autowired
    private ExpectationDao expectationDao;

    @SpyBean
    private WorkflowService workflowService;

    @MockBean
    private WorkflowClient workflowClient;

    @SpyBean
    private AbnormalDatasetService abnormalDatasetService;

    @SpyBean
    private EventPublisher publisher;

    @SpyBean
    private DatasetRepository datasetRepository;

    @MockBean
    private MetadataClient metadataClient;

    @MockBean
    private DataQualityCheckHook checkHook;

    @BeforeEach
    public void mock() throws IOException {
        doAnswer(invocation -> {
            Long taskId = invocation.getArgument(0, Long.class);
            TaskRun taskRun = TaskRun.newBuilder().withTask(Task.newBuilder().withId(taskId).build())
                    .withId(WorkflowIdGenerator.nextTaskRunId()).build();
            return taskRun;
        }).when(workflowClient).executeTask(anyLong(), any());

        doAnswer(invocation -> {
            Task task = invocation.getArgument(0, Task.class);
            Task createdTask = task.cloneBuilder().withId(WorkflowIdGenerator.nextTaskId()).build();
            return createdTask;
        }).when(workflowClient).createTask(any(Task.class));

        doReturn(MockOperatorFactory.createOperator())
                .when(workflowClient)
                .saveOperator(anyString(), any());

        doReturn(Optional.of(MockOperatorFactory.createOperator())).when(workflowClient).getOperator(anyString());

        doReturn(MockOperatorFactory.createOperator()).when(workflowClient).getOperator(anyLong());

        doAnswer(invocation -> {
            Long datasetId = invocation.getArgument(0,Long.class);
            Dataset dataset = Dataset.newBuilder()
                    .withGid(datasetId)
                    .withName("table" + datasetId)
                    .withDataStore(new HiveTableStore("hdfs://warehouse/database/table","database","table" + datasetId))
                    .build();
            return dataset;
        }).when(metadataClient).fetchDatasetById(anyLong());

        doAnswer(invocation -> {
            List<Long> datasetIds = invocation.getArgument(0,List.class);
            List<Dataset> datasetList = datasetIds.stream().map(datasetId ->
                Dataset.newBuilder()
                        .withGid(datasetId)
                        .withName("table" + datasetId)
                        .withDataStore(new HiveTableStore("hdfs://warehouse/database/table","database","table" + datasetId))
                        .build()
            ).collect(Collectors.toList());
            return datasetList;
        }).when(metadataClient).fetchDatasetsByIds(anyList());

        doReturn(true).when(checkHook).afterAll(any(DataQualityContext.class));
    }

    @Test
    public void handleCheckEvent_should_invoke_related_test_case() {
        //prepare : dataset1 related to case1 and case2,
        //dataset2 related to case3,dataset3 not related to any case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        Long dateset3Id = IdGenerator.getInstance().nextId();
        //case1
        Expectation expectation1 = MockExpectationFactory.create(dateset1Id);
        Long case1Id = expectation1.getExpectationId();
        expectationDao.create(expectation1);
        expectationDao.createRelatedDataset(case1Id, Lists.newArrayList(dateset1Id));
        //case2
        Expectation expectation2 = MockExpectationFactory.create(dateset1Id);
        Long case2Id = expectation2.getExpectationId();
        expectationDao.create(expectation2);
        expectationDao.createRelatedDataset(case2Id, Lists.newArrayList(dateset1Id));
        //case3
        Expectation expectation3 = MockExpectationFactory.create(dateset2Id);
        Long case3Id = expectation3.getExpectationId();
        expectationDao.create(expectation3);
        expectationDao.createRelatedDataset(case3Id, Lists.newArrayList(dateset2Id));

        //prepare event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id, dateset3Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        eventSubscriber.receiveEvent(event);

        //verify
        verify(workflowService,times(2)).executeTasks(captor.capture(),anyMap());
        List<Long> caseIds = captor.getAllValues().stream().flatMap(Collection::stream).collect(Collectors.toList());
        assertThat(caseIds, hasSize(2));
        assertThat(caseIds, containsInAnyOrder(case1Id, case2Id));

    }

    @Test
    public void handleCheckEventNoOutput_should_not_invoke_test_case() {
        // mock
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());

        //prepare case
        List<Long> relatedTableIds = new ArrayList<>();
        Long primaryDatasetId = IdGenerator.getInstance().nextId();
        relatedTableIds.add(primaryDatasetId);

        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create(relatedTableIds, primaryDatasetId);
        dataQualityService.createExpectation(expectationRequest);

        //prepare event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), new ArrayList<>());

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        eventSubscriber.receiveEvent(event);

        //verify
        verify(workflowService, times(0)).executeTasks(captor.capture());

    }

    @Test
    public void handleCaseFinishEvent_should_update_status() {
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        Expectation expectation1 = MockExpectationFactory.create(dateset1Id);
        Long case1Id = expectation1.getExpectationId();
        expectationDao.create(expectation1);
        expectationDao.createRelatedDataset(case1Id, Lists.newArrayList(dateset1Id));
        //case2
        Expectation expectation2 = MockExpectationFactory.create(dateset2Id);
        Long case2Id = expectation2.getExpectationId();
        expectationDao.create(expectation2);
        expectationDao.createRelatedDataset(case2Id, Lists.newArrayList(dateset2Id));

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id, dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0), true);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(1), false);
        Long taskId = WorkflowIdGenerator.nextTaskId();
        TaskAttemptFinishedEvent successEvent = new TaskAttemptFinishedEvent(taskAttemptId, taskId,
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        TaskAttemptFinishedEvent failedEvent = new TaskAttemptFinishedEvent(taskAttemptId, taskId,
                caseRunIds.get(1), TaskRunStatus.FAILED, new ArrayList<>(), new ArrayList<>());

        eventSubscriber.receiveEvent(successEvent);
        eventSubscriber.receiveEvent(failedEvent);

        //verify
        String case1Status = dataQualityRepository.fetchCaseRunStatus(caseRunIds.get(0));
        String case2Status = dataQualityRepository.fetchCaseRunStatus(caseRunIds.get(1));

        assertThat(case1Status, is(DataQualityStatus.SUCCESS.name()));
        assertThat(case2Status, is(DataQualityStatus.FAILED.name()));
    }

    @Test
    public void handleBlockCaseFailed_should_send_failed_event() {
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        Expectation expectation1 = MockExpectationFactory.create(dateset1Id).cloneBuilder()
                .withCaseType(CaseType.BLOCK).build();
        Long case1Id = expectation1.getExpectationId();
        expectationDao.create(expectation1);
        expectationDao.createRelatedDataset(case1Id, Lists.newArrayList(dateset1Id));
        //case2
        Expectation expectation2 = MockExpectationFactory.create(dateset2Id);
        Long case2Id = expectation2.getExpectationId();
        expectationDao.create(expectation2);
        expectationDao.createRelatedDataset(case2Id, Lists.newArrayList(dateset2Id));

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id, dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);


        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0), false);
        TaskAttemptFinishedEvent failedEvent = new TaskAttemptFinishedEvent(taskAttemptId, expectation1.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(failedEvent);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(1), true);
        TaskAttemptFinishedEvent successEvent = new TaskAttemptFinishedEvent(taskAttemptId, expectation2.getTaskId(),
                caseRunIds.get(1), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(successEvent);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher).publish(eventCaptor.capture());

        CheckResultEvent checkResultEvent = (CheckResultEvent) eventCaptor.getValue();
        assertThat(checkResultEvent.getCheckStatus(), is(false));
        assertThat(checkResultEvent.getTaskRunId(), is(taskRunId));

    }

    @Test
    public void handleOneCaseSuccess_should_not_send_event() {
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        Expectation expectation1 = MockExpectationFactory.create(dateset1Id).cloneBuilder()
                .withCaseType(CaseType.BLOCK).build();
        Long case1Id = expectation1.getExpectationId();
        expectationDao.create(expectation1);
        expectationDao.createRelatedDataset(case1Id, Lists.newArrayList(dateset1Id));
        //case2
        Expectation expectation2 = MockExpectationFactory.create(dateset2Id).cloneBuilder()
                .withCaseType(CaseType.FINAL_SUCCESS).build();
        Long case2Id = expectation2.getExpectationId();
        expectationDao.create(expectation2);
        expectationDao.createRelatedDataset(case2Id, Lists.newArrayList(dateset2Id));

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id, dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0), true);
        TaskAttemptFinishedEvent successEvent = new TaskAttemptFinishedEvent(taskAttemptId, expectation1.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());

        eventSubscriber.receiveEvent(successEvent);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher, times(0)).publish(eventCaptor.capture());

    }

    @Test
    public void handleNoCase_should_send_success_event() {
        //prepare dataset
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id, dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);


        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher, times(1)).publish(eventCaptor.capture());

        CheckResultEvent checkResultEvent = (CheckResultEvent) eventCaptor.getValue();
        assertThat(checkResultEvent.getCheckStatus(), is(true));
        assertThat(checkResultEvent.getTaskRunId(), is(taskRunId));

    }

    @Test
    public void handleAllCaseSuccess_should_send_success_event() {
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        Expectation expectation1 = MockExpectationFactory.create(dateset1Id).cloneBuilder()
                .withCaseType(CaseType.BLOCK).build();
        Long case1Id = expectation1.getExpectationId();
        expectationDao.create(expectation1);
        expectationDao.createRelatedDataset(case1Id, Lists.newArrayList(dateset1Id));
        //case2
        Expectation expectation2 = MockExpectationFactory.create(dateset2Id).cloneBuilder()
                .withCaseType(CaseType.FINAL_SUCCESS).build();
        Long case2Id = expectation2.getExpectationId();
        expectationDao.create(expectation2);
        expectationDao.createRelatedDataset(case2Id, Lists.newArrayList(dateset2Id));

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id, dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0), true);
        TaskAttemptFinishedEvent successEvent1 = new TaskAttemptFinishedEvent(taskAttemptId, expectation1.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(successEvent1);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(1), true);
        TaskAttemptFinishedEvent successEvent2 = new TaskAttemptFinishedEvent(taskAttemptId, expectation2.getTaskId(),
                caseRunIds.get(1), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(successEvent2);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher).publish(eventCaptor.capture());

        CheckResultEvent checkResultEvent = (CheckResultEvent) eventCaptor.getValue();
        assertThat(checkResultEvent.getCheckStatus(), is(true));
        assertThat(checkResultEvent.getTaskRunId(), is(taskRunId));
    }

    @Test
    public void onlyNonBlockingCaseFailed_should_send_success_event() {
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        Expectation expectation = MockExpectationFactory.create(dateset1Id);
        Expectation nonBlockingExpectation = expectation.cloneBuilder().withCaseType(CaseType.SKIP).build();
        Long caseId = nonBlockingExpectation.getExpectationId();
        expectationDao.create(nonBlockingExpectation);
        expectationDao.createRelatedDataset(caseId, Lists.newArrayList(dateset1Id));
        //case1
        Expectation expectation1 = MockExpectationFactory.create(dateset2Id).cloneBuilder()
                .withCaseType(CaseType.BLOCK).build();
        Long case1Id = expectation1.getExpectationId();
        expectationDao.create(expectation1);
        expectationDao.createRelatedDataset(case1Id, Lists.newArrayList(dateset2Id));

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);


        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0), false);
        TaskAttemptFinishedEvent failedEvent = new TaskAttemptFinishedEvent(taskAttemptId, nonBlockingExpectation.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.FAILED, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(failedEvent);

        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(1), true);
        TaskAttemptFinishedEvent successEvent = new TaskAttemptFinishedEvent(taskAttemptId, expectation1.getTaskId(),
                caseRunIds.get(1), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());

        eventSubscriber.receiveEvent(successEvent);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher).publish(eventCaptor.capture());

        CheckResultEvent checkResultEvent = (CheckResultEvent) eventCaptor.getValue();
        assertThat(checkResultEvent.getCheckStatus(), is(true));
        assertThat(checkResultEvent.getTaskRunId(), is(taskRunId));
    }

    @Test
    public void testDoSubscribe_RandomMockEvent() {
        Event nonRelatedEvent = new RandomMockEvent();
        eventSubscriber.receiveEvent(nonRelatedEvent);
        verify(abnormalDatasetService, never()).handleTaskRunCreatedEvent(any());
    }

    @Test
    public void testDoSubscribe_TaskRunCreatedEvent() {
        // mock
        doAnswer(invocation -> {
            Long taskRunId = invocation.getArgument(0, Long.class);
            TaskRun taskRun = TaskRun.newBuilder().withId(taskRunId)
                    .withTask(Task.newBuilder().withId(taskRunId + 1).withName("test_task").build())
                    .withStatus(TaskRunStatus.SUCCESS)
                    .withQueueName("default")
                    .build();
            return taskRun;
        }).when(workflowClient).getTaskRun(anyLong());
        doReturn(ImmutableSet.of(new DatasetInfo(IdGenerator.getInstance().nextId(), "test_dataset"))).when(workflowClient).fetchOutletNodes(anyLong());

        // post event
        TaskRunCreatedEvent taskRunCreatedEvent = new TaskRunCreatedEvent(IdGenerator.getInstance().nextId(), IdGenerator.getInstance().nextId());
        eventSubscriber.receiveEvent(taskRunCreatedEvent);

        verify(abnormalDatasetService, times(1)).handleTaskRunCreatedEvent(any());
        verify(workflowClient, times(1)).getTaskRun(any());
        verify(workflowClient, times(1)).fetchOutletNodes(any());
    }

    @Test
    public void testDoSubscribe_TaskRunCreatedEvent_IgnoredQueueName() {
        // mock
        doAnswer(invocation -> {
            Long taskRunId = invocation.getArgument(0, Long.class);
            TaskRun taskRun = TaskRun.newBuilder().withId(taskRunId)
                    .withTask(Task.newBuilder().withId(taskRunId + 1).withName("test_task").build())
                    .withStatus(TaskRunStatus.FAILED)
                    .withQueueName("metadata")
                    .build();
            return taskRun;
        }).when(workflowClient).getTaskRun(anyLong());

        // post event
        TaskRunCreatedEvent taskRunCreatedEvent = new TaskRunCreatedEvent(IdGenerator.getInstance().nextId(), IdGenerator.getInstance().nextId());
        eventSubscriber.receiveEvent(taskRunCreatedEvent);

        verify(workflowClient, times(1)).getTaskRun(anyLong());
        verify(workflowClient, never()).fetchOutletNodes(anyLong());

    }

    /**
     * A dummy mock event type to test response of listener (expect to take no effect)
     */
    private static class RandomMockEvent extends PrivateEvent {
    }

}
