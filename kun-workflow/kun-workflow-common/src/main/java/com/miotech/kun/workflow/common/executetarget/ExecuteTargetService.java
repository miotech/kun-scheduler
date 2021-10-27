package com.miotech.kun.workflow.common.executetarget;

import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

public class ExecuteTargetService {

    private final ExecuteTargetDao executeTargetDao;

    private final TargetProvider targetProvider;

    public ExecuteTargetService(ExecuteTargetDao executeTargetDao, TargetProvider targetProvider) {
        this.executeTargetDao = executeTargetDao;
        this.targetProvider = targetProvider;
    }

    public ExecuteTarget createExecuteTarget(ExecuteTarget executeTarget){
        return null;
    }
    public ExecuteTarget updateExecuteTarget(ExecuteTarget executeTarget){
        return null;
    }
    public ExecuteTarget fetchExecuteTarget(Long id){
        return null;
    }
    public ExecuteTarget fetchExecuteTarget(String name){
        return null;
    }
    public ExecuteTarget fetchExecuteTargets(){
        return null;
    }
    public ExecuteTarget getDefaultTarget(){
        return targetProvider.getDefaultTarget();
    }

}
