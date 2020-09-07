package com.miotech.kun.workflow.common.lineage.service;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;

import java.util.Optional;

public interface MetadataFacade {
    Optional<Dataset> fetchDatasetByDatastore(DataStore dataStore);
}
