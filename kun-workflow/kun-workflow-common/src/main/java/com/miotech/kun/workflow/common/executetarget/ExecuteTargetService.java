package com.miotech.kun.workflow.common.executetarget;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

import java.util.List;

@Singleton
public class ExecuteTargetService {

    private final ExecuteTargetDao executeTargetDao;

    private final TargetProvider targetProvider;

    @Inject
    public ExecuteTargetService(ExecuteTargetDao executeTargetDao, TargetProvider targetProvider) {
        this.executeTargetDao = executeTargetDao;
        this.targetProvider = targetProvider;
    }

    public ExecuteTarget createExecuteTarget(ExecuteTarget executeTarget) {
        ExecuteTarget savedTarget = fetchExecuteTarget(executeTarget.getName());
        if (savedTarget != null) {
            throw new IllegalArgumentException("target name = " + executeTarget.getName() + "is exist");
        }
        return executeTargetDao.createExecuteTarget(executeTarget);
    }

    public ExecuteTarget updateExecuteTarget(ExecuteTarget executeTarget) {
        ExecuteTarget savedTarget =
                fetchExecuteTarget(executeTarget.getName());
        if (savedTarget != null || !savedTarget.getId().equals(executeTarget.getId())) {
            throw new IllegalArgumentException("target name = " + executeTarget.getName() + "is exist");
        }
        return executeTargetDao.updateExecuteTarget(executeTarget);
    }

    public ExecuteTarget fetchExecuteTarget(Long id) {
        return executeTargetDao.fetchExecuteTarget(id);
    }

    public ExecuteTarget fetchExecuteTarget(String name) {
        return executeTargetDao.fetchExecuteTarget(name);
    }

    public List<ExecuteTarget> fetchExecuteTargets() {
        return executeTargetDao.fetchExecuteTargets();
    }

    public ExecuteTarget getDefaultTarget() {
        return targetProvider.getDefaultTarget();
    }

}
