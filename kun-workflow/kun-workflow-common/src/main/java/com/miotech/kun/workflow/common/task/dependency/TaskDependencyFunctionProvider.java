package com.miotech.kun.workflow.common.task.dependency;

import com.google.inject.Injector;
import com.miotech.kun.workflow.core.model.task.DependencyFunction;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskDependencyFunctionProvider {
    @Inject
    private Injector injector;

    public DependencyFunction from(String functionType) {
        switch (functionType) {
            case "latestTaskRun":
                return injector.getInstance(LatestTaskRunDependencyFunction.class);
            // TODO: Add other dependency functions
            default:
                throw new IllegalStateException(String.format("Unknown dependency function type: \"%s\"", functionType));
        }
    }
}
