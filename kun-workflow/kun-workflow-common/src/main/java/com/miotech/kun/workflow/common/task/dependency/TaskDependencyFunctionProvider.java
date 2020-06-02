package com.miotech.kun.workflow.common.task.dependency;

import com.miotech.kun.workflow.core.model.task.DependencyFunction;

public class TaskDependencyFunctionProvider {
    public static DependencyFunction getTaskDependencyFunctionFrom(String functionType) {
        // TODO: implement this
        switch (functionType) {
            default:
                return null;
        }
    }
}
