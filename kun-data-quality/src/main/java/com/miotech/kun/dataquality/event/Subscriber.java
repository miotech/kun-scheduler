package com.miotech.kun.dataquality.event;

import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.service.WorkflowService;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
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
        if(taskAttemptFinishedEvent.getFinalStatus().isSuccess()) {
            List<Long> datasetIds = taskAttemptFinishedEvent.getOutDataSetIds();
            if(datasetIds.isEmpty())
                return;
            List<Long> caseIds = dataQualityRepository.getWorkflowTasksByDatasetIds(datasetIds);
            if(!caseIds.isEmpty())
                workflowService.executeTasks(caseIds);
        }
    }
}
