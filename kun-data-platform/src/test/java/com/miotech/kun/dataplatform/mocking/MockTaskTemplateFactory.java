package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.Operator;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;

public class MockTaskTemplateFactory {
    private MockTaskTemplateFactory() {}

    public static TaskTemplate createTaskTemplate(Long operatorId) {
        return createTaskTemplates(1, operatorId).get(0);
    }

    public static List<TaskTemplate> createTaskTemplates(int num, Long operatorId) {
        List<TaskTemplate> taskTemplates = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            taskTemplates.add(TaskTemplate.newBuilder()
                    .withName("taskTemplate_" + i)
                    .withTemplateType("sql")
                    .withTemplateGroup("development")
                    .withOperator(Operator.newBuilder()
                            .withId(operatorId)
                    .build())
                    .withDefaultValues(ImmutableMap.of())
                    .withDisplayParameters(ImmutableList.of())
                    .build());
        }
        return taskTemplates;
    }
}
