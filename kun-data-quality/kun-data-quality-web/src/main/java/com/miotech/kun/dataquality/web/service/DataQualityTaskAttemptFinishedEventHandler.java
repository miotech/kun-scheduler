package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import com.miotech.kun.dataquality.core.hooks.DataQualityCheckHook;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.core.model.DataQualityContext;
import com.miotech.kun.dataquality.web.model.entity.ExpectationBasic;
import com.miotech.kun.dataquality.core.model.ValidateResult;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class DataQualityTaskAttemptFinishedEventHandler implements TaskAttemptFinishedEventHandler {

    @Autowired
    DataQualityService dataQualityService;

    @Autowired
    private ExpectationService expectationService;

    @Autowired
    @Qualifier("dataQuality-publisher")
    EventPublisher publisher;

    @Autowired
    private MetadataClient metadataClient;

    @Autowired
    private DataQualityCheckHook dataQualityCheckHook;

    @Override
    public void handle(TaskAttemptFinishedEvent taskAttemptFinishedEvent) {
        log.info("handle task attempt finish event = {}", taskAttemptFinishedEvent);
        //handle testcase
        ExpectationBasic expectationBasic = expectationService.fetchCaseBasicByTaskId(taskAttemptFinishedEvent.getTaskId());
        if (expectationBasic == null) {
            return;
        }
        long caseRunId = taskAttemptFinishedEvent.getTaskRunId();
        CaseRun caseRun = dataQualityService.fetchCaseRunByCaseRunId(caseRunId);
        Long taskAttemptId = caseRun.getTaskAttemptId();

        boolean caseStatus = taskAttemptFinishedEvent.getFinalStatus().isSuccess();
        // case failed because of an exception
        if (!caseStatus) {
            log.debug("case has failed ,update case status to failed");
            updateCaseToFailed(caseRun);
        }

        log.info("going check validate result for taskAttempt : {}", taskAttemptId);
        ValidateResult validateResult = checkValidateResult(taskAttemptId);
        log.info("validate result for taskAttempt : {} is {}", taskAttemptId, validateResult.name());
        Long datasetId = caseRun.getValidateDatasetId();
        Dataset dataset = metadataClient.fetchDatasetById(datasetId);
        DataQualityContext context = new DataQualityContext(dataset, caseRun.getValidateVersion(), validateResult);
        if (dataQualityCheckHook != null && validateResult.isFinished()) {
            dataQualityCheckHook.afterAll(context);
        }
        if (expectationBasic.getCaseType().equals(CaseType.SKIP)) {
            return;
        }
        switch (validateResult) {
            case RUNNING:
                onChecking(caseRun);
                break;
            case BLOCK_CASE_FAILED:
                onBlockFailed(caseRun);
                break;
            case FINAL_SUCCESS_FAILED:
                onUsingLatestFailed(caseRun);
                break;
            default:
                onSuccess(caseRun);
                break;
        }
    }

    private ValidateResult checkValidateResult(Long taskAttemptId) {
        return expectationService.validateTaskAttemptTestCase(taskAttemptId, Arrays.asList(CaseType.FINAL_SUCCESS, CaseType.BLOCK));
    }

    private void onChecking(CaseRun caseRun) {
        log.info("taskRunId = {} has test case not pass yet", caseRun.getTaskRunId());
    }

    private void onBlockFailed(CaseRun caseRun) {
        Long caseRunId = caseRun.getCaseRunId();
        Long taskRunId = caseRun.getTaskRunId();
        log.info("caseRunId = {} for taskRunId ={} is failed", caseRunId, taskRunId);
        sendDataQualityEvent(taskRunId, false);

    }

    private void onUsingLatestFailed(CaseRun caseRun) {
        Long caseRunId = caseRun.getCaseRunId();
        Long taskRunId = caseRun.getTaskRunId();
        log.info("caseRunId = {} for taskRunId ={} is failed", caseRunId, taskRunId);
        sendDataQualityEvent(taskRunId, true);

    }

    private void onSuccess(CaseRun caseRun) {
        sendDataQualityEvent(caseRun.getTaskRunId(), true);
    }


    private void sendDataQualityEvent(Long taskRunId, Boolean checkStatus) {
        CheckResultEvent event = new CheckResultEvent(taskRunId, checkStatus);
        log.info("send checkResult event = {} to infra", event);
        publisher.publish(event);
    }

    private void updateCaseToFailed(CaseRun caseRun) {
        caseRun.setStatus(DataQualityStatus.FAILED);
        dataQualityService.updateCaseRunStatus(caseRun);
    }

}
