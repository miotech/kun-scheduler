package com.miotech.kun.dataquality;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.mock.MockDataQualityFactory;
import com.miotech.kun.dataquality.mock.MockSubscriber;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.service.WorkflowService;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SubscriberTest extends DataQualityTestBase {

    @Autowired
    private MockSubscriber eventSubscriber;

    @Autowired
    private DataQualityRepository dataQualityRepository;

    @SpyBean
    private WorkflowService workflowService;

    @SpyBean
    private EventPublisher publisher;


    @Test
    public void handleCheckEvent_should_invoke_related_test_case() {
        //prepare : dataset1 related to case1 and case2,
        //dataset2 related to case3,dataset3 not related to any case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        Long dateset3Id = IdGenerator.getInstance().nextId();
        //case1
        DataQualityRequest dataQuality1Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        Long case1Id = dataQualityRepository.addCase(dataQuality1Request);
        //case2
        DataQualityRequest dataQuality2Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        Long case2Id = dataQualityRepository.addCase(dataQuality2Request);
        //case2
        DataQualityRequest dataQuality3Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset2Id), dateset2Id);
        dataQualityRepository.addCase(dataQuality3Request);

        //prepare event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset3Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        eventSubscriber.receiveEvent(event);

        //verify
        verify(workflowService).executeTasks(captor.capture());
        List<Long> caseIds = captor.getValue();
        assertThat(caseIds, hasSize(2));
        assertThat(caseIds,containsInAnyOrder(case1Id,case2Id));

    }

    @Test
    public void handleCheckEventNoOutput_should_not_invoke_test_case() {
        //prepare case
        List<Long> relatedTableIds = new ArrayList<>();
        Long primaryDatasetId = IdGenerator.getInstance().nextId();
        relatedTableIds.add(primaryDatasetId);
        DataQualityRequest dataQualityRequest = MockDataQualityFactory.createRequestWithRelatedTable(relatedTableIds, primaryDatasetId);
        dataQualityRepository.addCase(dataQualityRequest);

        //prepare event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), new ArrayList<>());

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        eventSubscriber.receiveEvent(event);

        //verify
        verify(workflowService,times(0)).executeTasks(captor.capture());

    }

    @Test
    public void handleCaseFinishEvent_should_update_status(){
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        DataQualityRequest dataQuality1Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        Long case1Id = dataQualityRepository.addCase(dataQuality1Request);
        //case2
        DataQualityRequest dataQuality2Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset2Id), dateset2Id);
        Long case2Id = dataQualityRepository.addCase(dataQuality2Request);

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //verify
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).executeTasks(captor.capture());
        List<Long> caseIds = captor.getValue();
        assertThat(caseIds, hasSize(2));
        assertThat(caseIds,containsInAnyOrder(case1Id,case2Id));

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0),true);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(1),false);
        TaskAttemptFinishedEvent successEvent = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality1Request.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        TaskAttemptFinishedEvent failedEvent = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality1Request.getTaskId(),
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
    public void handleOneCaseFailed_should_send_failed_event(){
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        DataQualityRequest dataQuality1Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        Long case1Id = dataQualityRepository.addCase(dataQuality1Request);
        //case2
        DataQualityRequest dataQuality2Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset2Id), dateset2Id);
        Long case2Id = dataQualityRepository.addCase(dataQuality2Request);

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //verify
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).executeTasks(captor.capture());
        List<Long> caseIds = captor.getValue();
        assertThat(caseIds, hasSize(2));
        assertThat(caseIds,containsInAnyOrder(case1Id,case2Id));

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0),false);
        TaskAttemptFinishedEvent failedEvent = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality1Request.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());

        eventSubscriber.receiveEvent(failedEvent);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher).publish(eventCaptor.capture());

        CheckResultEvent checkResultEvent = (CheckResultEvent) eventCaptor.getValue();
        assertThat(checkResultEvent.getCheckStatus(),is(false));
        assertThat(checkResultEvent.getTaskRunId(),is(taskRunId));

    }

    @Test
    public void handleOneCaseSuccess_should_not_send_event(){
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        DataQualityRequest dataQuality1Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        Long case1Id = dataQualityRepository.addCase(dataQuality1Request);
        //case2
        DataQualityRequest dataQuality2Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset2Id), dateset2Id);
        Long case2Id = dataQualityRepository.addCase(dataQuality2Request);

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //verify
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).executeTasks(captor.capture());
        List<Long> caseIds = captor.getValue();
        assertThat(caseIds, hasSize(2));
        assertThat(caseIds,containsInAnyOrder(case1Id,case2Id));

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0),true);
        TaskAttemptFinishedEvent successEvent = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality1Request.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());

        eventSubscriber.receiveEvent(successEvent);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher,times(0)).publish(eventCaptor.capture());

    }

    @Test
    public void handleNoCase_should_send_success_event(){
        //prepare dataset
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);


        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher,times(1)).publish(eventCaptor.capture());

    }

    @Test
    public void handleAllCaseSuccess_should_send_success_event(){
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        Long dateset2Id = IdGenerator.getInstance().nextId();
        //case1
        DataQualityRequest dataQuality1Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        Long case1Id = dataQualityRepository.addCase(dataQuality1Request);
        //case2
        DataQualityRequest dataQuality2Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset2Id), dateset2Id);
        Long case2Id = dataQualityRepository.addCase(dataQuality2Request);

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id,dateset2Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //verify
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).executeTasks(captor.capture());
        List<Long> caseIds = captor.getValue();
        assertThat(caseIds, hasSize(2));
        assertThat(caseIds,containsInAnyOrder(case1Id,case2Id));

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0),true);
        TaskAttemptFinishedEvent successEvent1 = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality1Request.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(successEvent1);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(1),true);
        TaskAttemptFinishedEvent successEvent2 = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality2Request.getTaskId(),
                caseRunIds.get(1), TaskRunStatus.SUCCESS, new ArrayList<>(), new ArrayList<>());
        eventSubscriber.receiveEvent(successEvent2);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher).publish(eventCaptor.capture());

        CheckResultEvent checkResultEvent = (CheckResultEvent) eventCaptor.getValue();
        assertThat(checkResultEvent.getCheckStatus(),is(true));
        assertThat(checkResultEvent.getTaskRunId(),is(taskRunId));
    }

    @Test
    public void handleNonBlockingCaseFailed_should_not_send_event(){
        //prepare case
        Long dateset1Id = IdGenerator.getInstance().nextId();
        DataQualityRequest dataQuality1Request = MockDataQualityFactory.
                createRequestWithRelatedTable(Lists.newArrayList(dateset1Id), dateset1Id);
        dataQuality1Request.setIsBlocking(false);
       dataQualityRepository.addCase(dataQuality1Request);

        //prepare check event
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<Long> outputDateSets = Lists.newArrayList(dateset1Id);
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttemptId, taskRunId, new ArrayList<>(), outputDateSets);
        eventSubscriber.receiveEvent(event);

        //verify
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).executeTasks(captor.capture());
        List<Long> caseIds = captor.getValue();
        assertThat(caseIds, hasSize(1));

        //prepare finished event
        List<Long> caseRunIds = dataQualityRepository.fetchCaseRunsByTaskRunId(taskRunId);
        dataQualityRepository.updateCaseRunStatus(caseRunIds.get(0),false);
        TaskAttemptFinishedEvent failedEvent = new TaskAttemptFinishedEvent(taskAttemptId,dataQuality1Request.getTaskId(),
                caseRunIds.get(0), TaskRunStatus.FAILED, new ArrayList<>(), new ArrayList<>());

        eventSubscriber.receiveEvent(failedEvent);

        //verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(publisher,times(0)).publish(eventCaptor.capture());

    }

}
