package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbnormalDatasetTaskAttemptFinishedEventHandler implements TaskAttemptFinishedEventHandler {

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Override
    public void handle(TaskAttemptFinishedEvent event) {
        abnormalDatasetService.updateStatusByTaskRunId(event.getTaskRunId(), event.getFinalStatus().isSuccess() ? "SUCCESS" : "FAILED");
    }
}
