package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.workflow.core.model.lineage.DataStore;

public interface MetadataServiceFacade {
    static String getServiceName() {
        return "KUN_METADATA_SERVICE";
    }

    public Dataset getDatasetByDatastore(DataStore datastore);
}
