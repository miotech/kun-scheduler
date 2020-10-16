package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.MetadataDatasourceDao;
import com.miotech.kun.metadata.core.model.dto.DataSourceDTO;

@Singleton
public class MetadataDatasourceService {

    @Inject
    private MetadataDatasourceDao metadataDatasetDao;

    public DataSourceDTO getDataSourceById(Long datasourceId) {
        Preconditions.checkNotNull(datasourceId, "Argument `datasourceId` cannot be null");
        return metadataDatasetDao.getDataSourceById(datasourceId);
    }
}
