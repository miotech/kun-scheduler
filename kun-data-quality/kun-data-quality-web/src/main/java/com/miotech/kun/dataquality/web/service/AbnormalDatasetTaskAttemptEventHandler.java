package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbnormalDatasetTaskAttemptEventHandler implements TaskAttemptFinishedEventHandler, TaskAttemptStatusChangeEventHandler {

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Override
    public void handle(TaskAttemptFinishedEvent event) {
        abnormalDatasetService.updateStatusByTaskRunId(event.getTaskRunId(), event.getFinalStatus().isSuccess() ? "SUCCESS" : "FAILED");
    }

    @Override
    public void handle(TaskAttemptStatusChangeEvent event) {
        TaskRunStatus toStatus = event.getToStatus();
        if (toStatus.isUpstreamFailed()) {
            abnormalDatasetService.updateStatusByTaskRunId(event.getTaskRunId(), "FAILED");
        }
    }
}
