package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;

public interface MetadataServiceFacade {
    /**
     * Retrieve dataset model object by given datastore as search key.
     * @param datastore key datastore object
     * @return Dataset model object. Returns null if not found by datastore key.
     */
    Dataset getDatasetByDatastore(DataStore datastore);
}
