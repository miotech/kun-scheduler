package com.miotech.kun.metadata.load.tool;

import com.miotech.kun.workflow.core.model.entity.DataStore;

public interface DatasetGidOperator {

    long generate(DataStore dataStore);

    void change(DataStore dataStore, long gid);

}
