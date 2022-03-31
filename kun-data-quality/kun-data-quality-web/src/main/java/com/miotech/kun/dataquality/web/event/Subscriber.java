package com.miotech.kun.dataquality.web.event;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataquality.core.hooks.DataQualityCheckHook;
import com.miotech.kun.dataquality.core.model.DataQualityContext;
import com.miotech.kun.dataquality.core.model.OperatorHookParams;
import com.miotech.kun.dataquality.core.model.ValidateResult;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.web.service.*;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Subscriber {

    @Autowired
    @Qualifier("dataQuality-subscriber")
    private EventSubscriber workflowEventSubscriber;

    @Autowired
    DataQualityService dataQualityService;

    @Autowired
    WorkflowService workflowService;

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    @Qualifier("dataQuality-publisher")
    EventPublisher publisher;

    @Autowired
    private TaskAttemptFinishedEventHandlerManager taskAttemptFinishedEventHandlerManager;

    @Autowired
    private MetadataClient metadataClient;

    @Autowired
    private OperatorHookParams operatorHookParams;

    @Value("${data-quality.hooks.operator-check-hook.classname}")
    private String operatorHookClass;

    @Autowired
    private DataQualityCheckHook dataQualityCheckHook;

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        workflowEventSubscriber.subscribe(event -> {
            if (event instanceof TaskAttemptFinishedEvent) {
                TaskAttemptFinishedEvent taskAttemptFinishedEvent = (TaskAttemptFinishedEvent) event;
                taskAttemptFinishedEventHandlerManager.handle(taskAttemptFinishedEvent);
            }
            if (event instanceof TaskAttemptCheckEvent) {
                handleTaskCheckEvent((TaskAttemptCheckEvent) event);
            }
            if (event instanceof TaskRunCreatedEvent) {
                handleTaskRunCreatedEvent((TaskRunCreatedEvent) event);
            }
        });
    }

    private void handleTaskCheckEvent(TaskAttemptCheckEvent taskAttemptCheckEvent) {
        Long taskAttemptId = taskAttemptCheckEvent.getTaskAttemptId();
        Long taskRunId = taskAttemptCheckEvent.getTaskRunId();
        log.info("start dq test for task attempt: " + taskAttemptId);
        List<Long> datasetIds = taskAttemptCheckEvent.getOutDataSetIds();
        int caseRunNum = executeDatasetsTestCase(datasetIds, taskRunId, taskAttemptId);
        //no case to run , is considered to be success
        if (caseRunNum == 0) {
            sendDataQualityEvent(taskRunId, true);
        }
    }

    private void handleTaskRunCreatedEvent(TaskRunCreatedEvent event) {
        log.info("record taskRun created event, event: {}", JSONUtils.toJsonString(event));
        abnormalDatasetService.handleTaskRunCreatedEvent(event);
    }

    private void sendDataQualityEvent(Long taskRunId, Boolean checkStatus) {
        CheckResultEvent event = new CheckResultEvent(taskRunId, checkStatus);
        log.info("send checkResult event = {} to infra", event);
        publisher.publish(event);
    }

    private Integer executeDatasetsTestCase(List<Long> datasetIds, Long taskRunId, Long taskAttemptId) {
        if (datasetIds == null) {
            return 0;
        }
        int caseRunNum = 0;
        List<Dataset> datasetList = metadataClient.fetchDatasetsByIds(datasetIds);
        for (Dataset dataset : datasetList) {
            Long datasetId = dataset.getGid();
            List<Long> caseIds = dataQualityService.getWorkflowTasksByDatasetId(datasetId); // set dataset and version to task config // run dataquality task with config }
            Map<String, Object> taskConfig = new HashMap<>();
            taskConfig.put("validate-dataset", JSONUtils.toJsonString(dataset));
            taskConfig.put("operator-hook-class", operatorHookClass);
            taskConfig.put("operator-hook-params", JSONUtils.toJsonString(operatorHookParams.getParams()));
            if (caseIds.size() == 0) {
                DataQualityContext context = new DataQualityContext(dataset, null, ValidateResult.SUCCESS);
                dataQualityCheckHook.afterAll(context);
            }
            log.info("going to execute cases : {} for dataset : {} ", caseIds, datasetId);
            List<Long> caseRunIdList = workflowService.executeTasks(caseIds, taskConfig);
            List<CaseRun> caseRunList = new ArrayList<>();
            for (int i = 0; i < caseRunIdList.size(); i++) {
                CaseRun caseRun = new CaseRun();
                caseRun.setValidateDatasetId(datasetId);
                caseRun.setCaseRunId(caseRunIdList.get(i));
                caseRun.setTaskRunId(taskRunId);
                caseRun.setTaskAttemptId(taskAttemptId);
                caseRun.setCaseId(caseIds.get(i));
                caseRunList.add(caseRun);
            }
            caseRunNum += caseRunList.size();
            log.info("sava case : {} to database", caseRunNum);
            dataQualityService.insertCaseRunWithTaskRun(caseRunList);
        }
        return caseRunNum;

    }
}
