package com.miotech.kun.commons.query.service;

import com.miotech.kun.commons.query.QuerySite;
import com.miotech.kun.commons.query.model.MetadataConnectionInfo;
import com.miotech.kun.commons.query.persistence.MetadataRepository;

/**
 * @author: Jie Chen
 * @created: 2020/7/10
 */
public class MetadataService {

    private MetadataRepository metadataRepository;

    public MetadataService() {
        this.metadataRepository = new MetadataRepository();
    }

    public MetadataConnectionInfo getConnectionInfo(QuerySite querySite) {
        MetadataConnectionInfo connectionInfo = metadataRepository.getConnectionInfo(querySite.getDatasourceId());
        connectionInfo.setDatabaseName(querySite.getDatabaseName());
        return connectionInfo;
    }
}
