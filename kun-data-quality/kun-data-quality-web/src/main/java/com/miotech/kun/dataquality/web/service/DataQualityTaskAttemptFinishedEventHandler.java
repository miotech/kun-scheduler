package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataQualityTaskAttemptFinishedEventHandler implements TaskAttemptFinishedEventHandler {

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    @Qualifier("dataQuality-publisher")
    EventPublisher publisher;

    @Override
    public void handle(TaskAttemptFinishedEvent taskAttemptFinishedEvent) {
        log.info("handle task attempt finish event = {}", taskAttemptFinishedEvent);
        //handle testcase
        DataQualityCaseBasic dataQualityCaseBasic = expectationDao.fetchCaseBasicByTaskId(taskAttemptFinishedEvent.getTaskId());
        if (dataQualityCaseBasic == null) {
            return;
        }
        boolean caseStatus = taskAttemptFinishedEvent.getFinalStatus().isSuccess();
        long caseRunId = taskAttemptFinishedEvent.getTaskRunId();
        Long taskRunId = expectationDao.fetchTaskRunIdByCase(caseRunId);
        if (caseStatus) {
            DataQualityStatus checkStatus = expectationDao.validateTaskRunTestCase(taskRunId);
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
    }

    private void sendDataQualityEvent(CheckResultEvent event) {
        log.info("send checkResult event = {} to infra", event);
        publisher.publish(event);
    }

}
