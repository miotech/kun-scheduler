package com.miotech.kun.dataquality.event;

import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.service.WorkflowService;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
                handleTaskAttemptStatusChangeEvent((TaskAttemptFinishedEvent) event);
            }
        });
    }

    private void handleTaskAttemptStatusChangeEvent(TaskAttemptFinishedEvent statusChangeEvent) {
        //get datasets
        List<Long> datasetIds = new ArrayList<>();
        //get dq cases tasks associated with the datasets
        List<Long> caseIds = dataQualityRepository.getWorkflowTasksByDatasetIds(datasetIds);
        //call wf to execute
        workflowService.executeTasks(caseIds);
    }
}
