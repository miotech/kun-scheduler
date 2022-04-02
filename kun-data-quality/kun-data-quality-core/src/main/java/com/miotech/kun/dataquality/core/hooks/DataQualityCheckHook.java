package com.miotech.kun.dataquality.core.hooks;

import com.miotech.kun.dataquality.core.model.DataQualityContext;
import com.miotech.kun.dataquality.core.model.HookParams;

public interface DataQualityCheckHook {

    /**
     * init hook with hookParams before execute
     * @param hookParams
     */
    public void initialize(HookParams hookParams);

    /**
     * execute after all test case of a dataset has finished
     * @param context
     * @return
     */
    public boolean afterAll(DataQualityContext context);

}
