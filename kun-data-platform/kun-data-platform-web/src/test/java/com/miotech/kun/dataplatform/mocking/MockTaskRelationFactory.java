package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskRelation;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.util.*;

public class MockTaskRelationFactory {
    private MockTaskRelationFactory() {}

    public static TaskRelation createTaskRelation() {
        return createTaskRelations(1).get(0);
    }

    public static List<TaskRelation> createTaskRelations(int num) {
        List<TaskRelation> taskRelations = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            long upstreamId = DataPlatformIdGenerator.nextDefinitionId();
            long downstreamId = DataPlatformIdGenerator.nextDefinitionId();

            taskRelations.add(TaskRelation.newBuilder()
                    .withUpstreamId(upstreamId)
                    .withDownstreamId(downstreamId)
                    .withCreatedAt(DateTimeUtils.now())
                    .withUpdatedAt(DateTimeUtils.now())
                    .build());
        }
        return taskRelations;
    }
}
