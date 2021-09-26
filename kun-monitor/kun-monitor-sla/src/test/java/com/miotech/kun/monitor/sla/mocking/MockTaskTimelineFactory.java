package com.miotech.kun.monitor.sla.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.time.ZoneId;

public class MockTaskTimelineFactory {

    public static TaskTimeline createTaskTimeline(Long definitionId) {
        return createTaskTimeline(definitionId, null);
    }

    public static TaskTimeline createTaskTimeline(Long definitionId, Long rootDefinitionId) {
        SlaConfig slaConfig = MockSlaFactory.create();
        return TaskTimeline.newBuilder()
                .withTaskRunId(IdGenerator.getInstance().nextId())
                .withDefinitionId(definitionId)
                .withLevel(1)
                .withDeadline(slaConfig.getDeadline(ZoneId.systemDefault().getId()))
                .withRootDefinitionId(rootDefinitionId)
                .withCreatedAt(DateTimeUtils.now())
                .withUpdatedAt(DateTimeUtils.now())
                .build();
    }

}
