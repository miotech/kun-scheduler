package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;

/**
 * Exposed RPC service interface of metadata service module.
 * @author Josh Ouyang
 */
public interface MetadataServiceFacade {
    /**
     * Obtain dataset model object (from remote) by given datastore as search key.
     * @param datastore key datastore object
     * @return Dataset model object. Returns null if not found by datastore key.
     */
    Dataset getDatasetByDatastore(DataStore datastore);
}
