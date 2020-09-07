package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;

public interface MetadataServiceFacade {
    static String getServiceName() {
        return "KUN_METADATA_SERVICE";
    }

    public Dataset getDatasetByDatastore(DataStore datastore);
}
