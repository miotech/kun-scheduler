package com.miotech.kun.metadata.load.tool;

import com.miotech.kun.workflow.core.model.entity.DataStore;

public abstract class DatasetGidGenerator implements DatasetGidOperator {

    @Override
    public void change(DataStore dataStore, long gid) {
        // nothing
    }
}
