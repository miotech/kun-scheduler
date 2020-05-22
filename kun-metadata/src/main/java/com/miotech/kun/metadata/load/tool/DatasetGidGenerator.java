package com.miotech.kun.metadata.load.tool;

import com.miotech.kun.workflow.core.model.entity.DataStore;

public interface DatasetGidGenerator {

    long generate(DataStore dataStore);

}
