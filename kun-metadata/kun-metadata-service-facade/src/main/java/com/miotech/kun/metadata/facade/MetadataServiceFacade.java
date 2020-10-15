package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetBaseInfo;

import java.util.List;

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

    /**
     * Search for eligible datasets based on datasoureId and name like
     * @param datasourceId Identifier to locate the unique Datasource
     * @param name The `name` of Dataset
     * @return Basic information of Dataset
     */
    List<DatasetBaseInfo> fetchDatasetsByDatasourceAndNameLike(Long datasourceId, String name);

}
