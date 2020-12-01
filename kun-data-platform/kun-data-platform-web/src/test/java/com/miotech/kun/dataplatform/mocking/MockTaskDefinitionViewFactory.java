package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.util.ArrayList;

public class MockTaskDefinitionViewFactory  {
    public static TaskDefinitionView createTaskDefView() {
        return createTaskDefView(IdGenerator.getInstance().nextId());
    }

    public static TaskDefinitionView createTaskDefView(Long id) {
        return createTaskDefView(id, 1L);
    }

    public static TaskDefinitionView createTaskDefView(Long id, Long creator) {
        return TaskDefinitionView.newBuilder()
                .withId(id)
                .withName("view-" + id.toString())
                .withCreator(creator)
                .withUpdateTime(DateTimeUtils.now())
                .withCreateTime(DateTimeUtils.now())
                .withLastModifier(creator)
                .withIncludedTaskDefinitions(new ArrayList<>())
                .build();
    }
}
