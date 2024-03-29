package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

public class MockAbnormalDatasetFactory {

    private MockAbnormalDatasetFactory() {
    }

    public static AbnormalDataset create(Long taskRunId) {
        return AbnormalDataset.newBuilder()
                .withDatasetGid(IdGenerator.getInstance().nextId())
                .withTaskRunId(taskRunId)
                .withTaskId(IdGenerator.getInstance().nextId())
                .withTaskName("test_task")
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withScheduleAt("19700101")
                .build();
    }

    public static AbnormalDataset create() {
        return AbnormalDataset.newBuilder()
                .withDatasetGid(IdGenerator.getInstance().nextId())
                .withTaskRunId(IdGenerator.getInstance().nextId())
                .withTaskId(IdGenerator.getInstance().nextId())
                .withTaskName("test_task")
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withScheduleAt("19700101")
                .build();
    }

}
