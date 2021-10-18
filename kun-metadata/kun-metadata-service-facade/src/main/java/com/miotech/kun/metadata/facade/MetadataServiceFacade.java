package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;

/**
 * Exposed RPC service interface of metadata service module.
 * @author Josh Ouyang
 */
public interface MetadataServiceFacade {

    /**
     * fetch dataset by given datastore,if dataset not exist,
     * create new one and return
     * @param datastore
     * @return Dataset
     */
    Dataset createDataSetIfNotExist(DataStore datastore);

}
