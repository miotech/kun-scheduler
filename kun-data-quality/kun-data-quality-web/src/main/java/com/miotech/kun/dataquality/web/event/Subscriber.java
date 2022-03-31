package com.miotech.kun.dataquality.web.event;

import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetService;
import com.miotech.kun.dataquality.web.service.EventHandlerManager;
import com.miotech.kun.dataquality.web.service.WorkflowService;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
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
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    @Qualifier("dataQuality-publisher")
    EventPublisher publisher;

    @Autowired
    private EventHandlerManager eventHandlerManager;

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        workflowEventSubscriber.subscribe(event -> {
            if (event instanceof TaskAttemptCheckEvent) {
                handleTaskCheckEvent((TaskAttemptCheckEvent) event);
            }
            if (event instanceof TaskRunCreatedEvent) {
                handleTaskRunCreatedEvent((TaskRunCreatedEvent) event);
            }

            eventHandlerManager.handle(event);
        });
    }

    private void handleTaskCheckEvent(TaskAttemptCheckEvent taskAttemptCheckEvent) {
        Long taskAttemptId = taskAttemptCheckEvent.getTaskAttemptId();
        log.info("start dq test for task attempt: " + taskAttemptId);
        List<Long> datasetIds = taskAttemptCheckEvent.getOutDataSetIds();
        if (datasetIds.isEmpty()) {
            return;
        }
        log.info("get dq cases for datasetIds: " + taskAttemptCheckEvent.getOutDataSetIds());
        List<Long> caseIds = dataQualityRepository.getWorkflowTasksByDatasetIds(datasetIds);
        if (caseIds.isEmpty()) {
            log.debug("no test case for taskAttemptId = {} ",taskAttemptId);
            CheckResultEvent event = new CheckResultEvent(taskAttemptCheckEvent.getTaskRunId(), true);
            sendDataQualityEvent(event);
            return;
        }
        log.info("run dq test case: " + caseIds);
        List<Long> caseRunIdList = workflowService.executeTasks(caseIds);
        List<CaseRun> caseRunList = new ArrayList<>();
        for (int i = 0; i < caseRunIdList.size(); i++) {
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunIdList.get(i));
            caseRun.setTaskRunId(taskAttemptCheckEvent.getTaskRunId());
            caseRun.setTaskAttemptId(taskAttemptId);
            caseRun.setCaseId(caseIds.get(i));
            caseRunList.add(caseRun);
        }
        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);
    }

    private void handleTaskRunCreatedEvent(TaskRunCreatedEvent event) {
        log.info("record taskRun created event, event: {}", JSONUtils.toJsonString(event));
        abnormalDatasetService.handleTaskRunCreatedEvent(event);
    }

    private void sendDataQualityEvent(CheckResultEvent event) {
        log.info("send checkResult event = {} to infra", event);
        publisher.publish(event);
    }
}
