package com.miotech.kun.dataquality.event;

import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.service.WorkflowService;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

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

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        workflowEventSubscriber.subscribe(event -> {
            if (event instanceof TaskAttemptFinishedEvent) {
                handleTaskAttemptFinishedEvent((TaskAttemptFinishedEvent) event);
            }
        });
    }

    private void handleTaskAttemptFinishedEvent(TaskAttemptFinishedEvent taskAttemptFinishedEvent) {
        if (taskAttemptFinishedEvent.getFinalStatus().isSuccess()) {
            log.info("start dq test for task attempt: " + taskAttemptFinishedEvent.getAttemptId());
            List<Long> datasetIds = taskAttemptFinishedEvent.getOutDataSetIds();
            if (datasetIds.isEmpty()) {
                return;
            }
            log.info("get dq cases for datasetIds: " + taskAttemptFinishedEvent.getOutDataSetIds());
            List<Long> caseIds = dataQualityRepository.getWorkflowTasksByDatasetIds(datasetIds);
            if (!caseIds.isEmpty()) {
                log.info("run dq test case: " + caseIds);
                workflowService.executeTasks(caseIds);
            }
        }
    }
}
