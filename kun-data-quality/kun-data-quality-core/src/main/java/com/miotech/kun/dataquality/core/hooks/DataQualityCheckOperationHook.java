package com.miotech.kun.dataquality.core.hooks;

import com.miotech.kun.dataquality.core.model.DataQualityContext;
import com.miotech.kun.dataquality.core.model.DataQualityOperatorContext;
import com.miotech.kun.dataquality.core.model.OperatorHookParams;

public interface DataQualityCheckOperationHook {

    /**
     * init hook with hookParams before execute
     * @param hookParams
     */
    void initialize(OperatorHookParams hookParams);

    /**
     * execute before a test case
     * @param context
     * @return
     */
    boolean before(DataQualityOperatorContext context);

    /**
     * execute after a test case
     * @param context
     * @return
     */
    boolean after(DataQualityOperatorContext context);

}
