package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.model.taskdefinition.*;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.*;

import static com.miotech.kun.dataplatform.common.tasktemplate.dao.TaskTemplateDaoTest.TEST_TEMPLATE;

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
