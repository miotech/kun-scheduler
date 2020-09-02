package com.miotech.kun.workflow.common.lineage.service;

import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.Dataset;

import java.util.Optional;

public interface MetadataFacade {
    Optional<Dataset> fetchDatasetByDatastore(DataStore dataStore);
}
