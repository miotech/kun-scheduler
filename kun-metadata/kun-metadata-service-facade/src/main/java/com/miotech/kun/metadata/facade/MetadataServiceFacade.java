package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;

public interface MetadataServiceFacade {
    Dataset getDatasetByDatastore(DataStore datastore);

    String ping(String msg);
}
