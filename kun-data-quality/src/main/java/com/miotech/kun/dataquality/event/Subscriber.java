package com.miotech.kun.dataquality.event;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataquality.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.service.WorkflowService;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class Subscriber {

    @Autowired
    private EventSubscriber workflowEventSubscriber;

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    WorkflowService workflowService;

    @Autowired
    EventPublisher publisher;

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        workflowEventSubscriber.subscribe(event -> {
            if (event instanceof TaskAttemptFinishedEvent) {
                handleTaskAttemptFinishedEvent((TaskAttemptFinishedEvent) event);
            }
            if (event instanceof TaskAttemptCheckEvent) {
                handleTaskCheckEvent((TaskAttemptCheckEvent) event);
            }
        });
    }

    private void handleTaskAttemptFinishedEvent(TaskAttemptFinishedEvent taskAttemptFinishedEvent) {
        //handle testcase
        DataQualityCaseBasic dataQualityCaseBasic = dataQualityRepository.fetchCaseBasicByTaskId(taskAttemptFinishedEvent.getTaskId());
        if (dataQualityCaseBasic != null) {
            boolean caseStatus = taskAttemptFinishedEvent.getFinalStatus().isSuccess();
            long caseRunId = taskAttemptFinishedEvent.getTaskRunId();
            dataQualityRepository.updateCaseRunStatus(caseRunId, caseStatus);
            if (!dataQualityCaseBasic.getIsBlock()) {
                return;
            }
            Long taskRunId = dataQualityRepository.fetchTaskRunIdByCase(caseRunId);
            if (caseStatus) {
                boolean checkStatus = dataQualityRepository.validateTaskRunTestCase(taskRunId);
                if (checkStatus) {
                    CheckResultEvent event = new CheckResultEvent(taskRunId, true);
                    sendDataQualityEvent(event);
                }
                return;
            }
            CheckResultEvent event = new CheckResultEvent(taskRunId, false);
            sendDataQualityEvent(event);
            return;
        }
    }

    private void handleTaskCheckEvent(TaskAttemptCheckEvent taskAttemptCheckEvent) {
        log.info("start dq test for task attempt: " + taskAttemptCheckEvent.getTaskAttemptId());
        List<Long> datasetIds = taskAttemptCheckEvent.getOutDataSetIds();
        if (datasetIds.isEmpty()) {
            return;
        }
        log.info("get dq cases for datasetIds: " + taskAttemptCheckEvent.getOutDataSetIds());
        List<Long> caseIds = dataQualityRepository.getWorkflowTasksByDatasetIds(datasetIds);
        if (!caseIds.isEmpty()) {
            log.info("run dq test case: " + caseIds);
            List<Long> caseRunIdList = workflowService.executeTasks(caseIds);
            dataQualityRepository.insertCaseRunWithTaskRun(taskAttemptCheckEvent.getTaskRunId(),caseRunIdList);
        }
    }

    private void sendDataQualityEvent(CheckResultEvent event) {
        publisher.publish(event);
    }
}
