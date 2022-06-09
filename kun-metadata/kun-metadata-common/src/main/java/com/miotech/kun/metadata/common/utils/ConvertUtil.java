package com.miotech.kun.metadata.common.utils;

import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

import java.util.HashMap;
import java.util.Map;

public class ConvertUtil {


    /**
     * just used to compatible old data
     * will be removed after discovery refactor
     */
    public static ConnectionInfoV1 generateConnectionInfoV1(DataSource dataSource) {
        Map<String, Object> oldConfig = new HashMap<>();
        ConnectionConfig connectionConfig = dataSource.getConnectionConfig();
        ConnectionInfo userConnection = connectionConfig.getUserConnection();
        ConnectionInfo metaConnection = connectionConfig.getMetadataConnection();
        ConnectionInfo storageConnection = connectionConfig.getStorageConnection();
        if (dataSource.getDatasourceType().equals(DatasourceType.HIVE)) {
            if (userConnection instanceof HiveServerConnectionInfo) {
                oldConfig.put("dataStoreHost", ((HiveServerConnectionInfo) userConnection).getHost());
                oldConfig.put("dataStorePort", ((HiveServerConnectionInfo) userConnection).getPort());
                oldConfig.put("dataStoreUsername", ((HiveServerConnectionInfo) userConnection).getUsername());
                oldConfig.put("dataStorePassword", ((HiveServerConnectionInfo) userConnection).getPassword());
                if (metaConnection != null && metaConnection instanceof HiveMetaStoreConnectionInfo) {
                    oldConfig.put("metaStoreUris", ((HiveMetaStoreConnectionInfo) metaConnection).getMetaStoreUris());
                }
            } else if (userConnection instanceof AthenaConnectionInfo) {
                oldConfig.put("athenaUrl", ((AthenaConnectionInfo) userConnection).getAthenaUrl());
                oldConfig.put("athenaUsername", ((AthenaConnectionInfo) userConnection).getAthenaUsername());
                oldConfig.put("athenaPassword", ((AthenaConnectionInfo) userConnection).getAthenaPassword());
                if (metaConnection != null && metaConnection instanceof GlueConnectionInfo) {
                    oldConfig.put("glueRegion", ((GlueConnectionInfo) metaConnection).getGlueRegion());
                    oldConfig.put("glueAccessKey", ((GlueConnectionInfo) metaConnection).getGlueAccessKey());
                    oldConfig.put("glueSecretKey", ((GlueConnectionInfo) metaConnection).getGlueSecretKey());
                }
                if (storageConnection != null && storageConnection instanceof S3ConnectionInfo) {
                    oldConfig.put("s3Region", ((S3ConnectionInfo) storageConnection).getS3Region());
                    oldConfig.put("s3AccessKey", ((S3ConnectionInfo) storageConnection).getS3AccessKey());
                    oldConfig.put("s3SecretKey", ((S3ConnectionInfo) storageConnection).getS3SecretKey());
                }
            }
        } else if (userConnection instanceof PostgresConnectionInfo) {
            PostgresConnectionInfo connection = (PostgresConnectionInfo) userConnection;
            oldConfig.put("host", connection.getHost());
            oldConfig.put("port", connection.getPort());
            oldConfig.put("username", connection.getUsername());
            oldConfig.put("password", connection.getPassword());
        } else if (userConnection instanceof MysqlConnectionInfo) {
            MysqlConnectionInfo connection = (MysqlConnectionInfo) userConnection;
            oldConfig.put("host", connection.getHost());
            oldConfig.put("port", connection.getPort());
            oldConfig.put("username", connection.getUsername());
            oldConfig.put("password", connection.getPassword());
        } else if (userConnection instanceof MongoConnectionInfo) {
            MongoConnectionInfo connection = (MongoConnectionInfo) userConnection;
            oldConfig.put("host", connection.getHost());
            oldConfig.put("port", connection.getPort());
            oldConfig.put("username", connection.getUsername());
            oldConfig.put("password", connection.getPassword());
        } else if (userConnection instanceof ArangoConnectionInfo) {
            ArangoConnectionInfo connection = (ArangoConnectionInfo) userConnection;
            oldConfig.put("host", connection.getHost());
            oldConfig.put("port", connection.getPort());
            oldConfig.put("username", connection.getUsername());
            oldConfig.put("password", connection.getPassword());
        } else if (userConnection instanceof  ESConnectionInfo){
            ESConnectionInfo connection = (ESConnectionInfo) userConnection;
            oldConfig.put("host", connection.getHost());
            oldConfig.put("port", connection.getPort());
            oldConfig.put("username", connection.getUsername());
            oldConfig.put("password", connection.getPassword());
        }
        return new ConnectionInfoV1(oldConfig);
    }
}
