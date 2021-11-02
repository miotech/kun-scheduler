package com.miotech.kun.workflow.common.executetarget;

import com.google.inject.ImplementedBy;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

@ImplementedBy(DefaultTargetProvider.class)
public interface TargetProvider {

    /**
     * Return the default target used when
     * the user does not specify a target
     * @return
     */
    ExecuteTarget getDefaultTarget();

}
