package com.miotech.kun.workflow.common.executetarget;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

import javax.inject.Inject;

public class ExecuteTargetDao {


    private final DatabaseOperator dbOperator;

    @Inject
    public ExecuteTargetDao(DatabaseOperator dbOperator){
        this.dbOperator = dbOperator;
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
}
