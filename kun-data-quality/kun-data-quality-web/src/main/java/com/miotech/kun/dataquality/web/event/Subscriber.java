package com.miotech.kun.dataquality.web.event;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.web.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.service.WorkflowService;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Subscriber {

    @Autowired
    @Qualifier("dataQuality-subscriber")
    private EventSubscriber workflowEventSubscriber;

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    WorkflowService workflowService;

    @Autowired
    @Qualifier("dataQuality-publisher")
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
        log.info("handle task attempt finish event = {}", taskAttemptFinishedEvent);
        //handle testcase
        DataQualityCaseBasic dataQualityCaseBasic = dataQualityRepository.fetchCaseBasicByTaskId(taskAttemptFinishedEvent.getTaskId());
        if (dataQualityCaseBasic == null) {
            return;
        }
        boolean caseStatus = taskAttemptFinishedEvent.getFinalStatus().isSuccess();
        long caseRunId = taskAttemptFinishedEvent.getTaskRunId();
        Long taskRunId = dataQualityRepository.fetchTaskRunIdByCase(caseRunId);
        if (caseStatus) {
            DataQualityStatus checkStatus = dataQualityRepository.validateTaskRunTestCase(taskRunId);
            if (checkStatus.equals(DataQualityStatus.SUCCESS)) {
                log.info("taskRunId = {} all test case has pass", taskRunId);
                CheckResultEvent event = new CheckResultEvent(taskRunId, true);
                sendDataQualityEvent(event);
                return;
            }
            if (checkStatus.equals(DataQualityStatus.FAILED)) {
                log.info("caseRunId = {} for taskRunId ={} is failed", caseRunId, taskRunId);
                CheckResultEvent event = new CheckResultEvent(taskRunId, false);
                sendDataQualityEvent(event);
                return;
            }
            log.info("taskRunId = {} has test case not pass yet", taskRunId);
            return;
        }
        log.info("caseRunId = {} for taskRunId ={} is failed", caseRunId, taskRunId);
        if (!dataQualityCaseBasic.getIsBlocking()) {
            log.info("caseRun {} is non-blocking, skip.", caseRunId);
            return;
        }
        CheckResultEvent event = new CheckResultEvent(taskRunId, false);
        sendDataQualityEvent(event);
        return;
    }

    private void handleTaskCheckEvent(TaskAttemptCheckEvent taskAttemptCheckEvent) {
        log.info("start dq test for task attempt: " + taskAttemptCheckEvent.getTaskAttemptId());
        List<Long> datasetIds = taskAttemptCheckEvent.getOutDataSetIds();
        if (datasetIds.isEmpty()) {
            return;
        }
        log.info("get dq cases for datasetIds: " + taskAttemptCheckEvent.getOutDataSetIds());
        List<Long> caseIds = dataQualityRepository.getWorkflowTasksByDatasetIds(datasetIds);
        if (caseIds.isEmpty()) {
            Long taskRunId = taskAttemptCheckEvent.getTaskRunId();
            log.debug("no test case for taskRunId = {} ",taskRunId);
            CheckResultEvent event = new CheckResultEvent(taskRunId, true);
            sendDataQualityEvent(event);
            return;
        }
        log.info("run dq test case: " + caseIds);
        List<Long> caseRunIdList = workflowService.executeTasks(caseIds);
        Long taskRunId = taskAttemptCheckEvent.getTaskRunId();
        List<CaseRun> caseRunList = new ArrayList<>();
        for (int i = 0; i < caseRunIdList.size(); i++) {
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunIdList.get(i));
            caseRun.setTaskRunId(taskRunId);
            caseRun.setCaseId(caseIds.get(i));
            caseRunList.add(caseRun);
        }
        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);
    }

    private void sendDataQualityEvent(CheckResultEvent event) {
        log.info("send checkResult event = {} to infra", event);
        publisher.publish(event);
    }
}
