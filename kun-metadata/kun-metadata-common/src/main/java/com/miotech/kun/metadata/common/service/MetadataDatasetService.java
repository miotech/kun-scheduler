package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.core.model.Dataset;

import java.util.Optional;

public class MetadataDatasetService {
    private final MetadataDatasetDao metadataDatasetDao;

    @Inject
    public MetadataDatasetService(MetadataDatasetDao metadataDatasetDao) {
        this.metadataDatasetDao = metadataDatasetDao;
    }

    /**
     * Fetch dataset by its global id and returns an optional object.
     * @param gid global id
     * @return An optional Dataset object. Not present if not found.
     */
    public Optional<Dataset> fetchDatasetByGid(Long gid) {
        Preconditions.checkNotNull(gid, "Argument `gid` cannot be null");
        return metadataDatasetDao.fetchDatasetByGid(gid);
    }
}
