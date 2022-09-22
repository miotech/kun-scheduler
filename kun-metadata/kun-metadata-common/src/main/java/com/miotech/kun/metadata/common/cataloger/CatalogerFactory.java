package com.miotech.kun.metadata.common.cataloger;

import com.miotech.kun.metadata.common.client.*;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CatalogerFactory {

    private Logger logger = LoggerFactory.getLogger(CatalogerFactory.class);

    private final FieldMappingService fieldMappingService;

    private final ClientFactory clientFactory;

    @Inject
    public CatalogerFactory(FieldMappingService fieldMappingService, ClientFactory clientFactory) {
        this.fieldMappingService = fieldMappingService;
        this.clientFactory = clientFactory;
    }

    public Cataloger generateCataloger(DataSource dataSource) {
        DatasourceConnection datasourceConnection = dataSource.getDatasourceConnection();
        ConnectionInfo metaConnection = datasourceConnection.getMetadataConnection();
        ConnectionInfo storageConnection = datasourceConnection.getStorageConnection();
        MetadataBackend metadataBackend = createMetaBackend(metaConnection.getConnectionConfigInfo());
        StorageBackend storageBackend = createStorage(storageConnection.getConnectionConfigInfo());
        logger.debug("metaConnectionInfo is {}\n storageConnectionInfo is {}", metaConnection, storageConnection);
        return new Cataloger(metadataBackend, storageBackend);
    }

    private MetadataBackend createMetaBackend(ConnectionConfigInfo connectionConfigInfo) {
        ConnectionType metaType = connectionConfigInfo.getConnectionType();
        switch (metaType) {
            case GLUE:
                return new GlueBackend((GlueConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
            case HIVE_THRIFT:
                return new HiveThriftBackend((HiveMetaStoreConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
            case POSTGRESQL:
                return new PostgresBackend((PostgresConnectionConfigInfo) connectionConfigInfo, fieldMappingService);
            default:
                throw new IllegalStateException("metadata type : " + metaType + " not support yet");
        }

    }

    private StorageBackend createStorage(ConnectionConfigInfo connectionConfigInfo) {
        ConnectionType storageType = connectionConfigInfo.getConnectionType();
        switch (storageType) {
            case S3:
                return new S3Backend((S3ConnectionConfigInfo) connectionConfigInfo, clientFactory);
            case HIVE_THRIFT:
                return new HiveThriftBackend((HiveMetaStoreConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
            case POSTGRESQL:
                return new PostgresBackend((PostgresConnectionConfigInfo) connectionConfigInfo, fieldMappingService);
            default:
                throw new IllegalStateException("storage type : " + storageType + " not support yet");
        }

    }
}
