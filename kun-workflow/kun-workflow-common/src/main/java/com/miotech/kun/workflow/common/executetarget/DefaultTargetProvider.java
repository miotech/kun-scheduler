package com.miotech.kun.workflow.common.executetarget;

import com.google.inject.Inject;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

public class DefaultTargetProvider implements TargetProvider{

    private final ExecuteTargetDao executeTargetDao;

    private final String DEFAULT_TARGET_NAME = "prod";

    private ExecuteTarget defaultTarget;

    @Inject
    public DefaultTargetProvider(ExecuteTargetDao executeTargetDao) {
        this.executeTargetDao = executeTargetDao;
    }


    @Override
    public ExecuteTarget getDefaultTarget() {
        if(defaultTarget == null){
            defaultTarget = executeTargetDao.fetchExecuteTarget(DEFAULT_TARGET_NAME);
        }
        return defaultTarget;
    }
}
