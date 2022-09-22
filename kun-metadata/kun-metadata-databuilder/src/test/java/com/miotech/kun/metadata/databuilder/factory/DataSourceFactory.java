package com.miotech.kun.metadata.databuilder.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;

import java.util.HashMap;
import java.util.Map;

public class DataSourceFactory {

    private DataSourceFactory() {
    }


    public static DatasourceConnection createDatasourceConnection(ConnectionConfigInfo commConnectionConfigInfo) {
        ConnectionInfo user_connection = createConnectionInfo("user_connection", ConnScope.USER_CONN, commConnectionConfigInfo);
        ConnectionInfo data_connection = createConnectionInfo("data_connection", ConnScope.DATA_CONN, commConnectionConfigInfo);
        ConnectionInfo meta_connection = createConnectionInfo("meta_connection", ConnScope.METADATA_CONN, commConnectionConfigInfo);
        ConnectionInfo storage_connection = createConnectionInfo("storage_connection", ConnScope.STORAGE_CONN, commConnectionConfigInfo);
        return DatasourceConnection
                .newBuilder()
                .withUserConnectionList(Lists.newArrayList(user_connection))
                .withDataConnection(data_connection)
                .withMetadataConnection(meta_connection)
                .withStorageConnection(storage_connection)
                .build();
    }

    public static ConnectionInfo createConnectionInfo(
            String name,
            Long datasourceId,
            ConnScope connScope,
            ConnectionConfigInfo connectionConfigInfo
    ) {
        String user = "test_user";
        String description = "test_description";
        return ConnectionInfo.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withDatasourceId(datasourceId)
                .withConnScope(connScope)
                .withName(name)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withDescription(description)
                .withCreateUser(user)
                .withUpdateUser(user)
                .build();
    }

    public static ConnectionInfo createConnectionInfo(
            String name,
            ConnScope connScope,
            ConnectionConfigInfo connectionConfigInfo
    ) {
        String user = "test_user";
        String description = "test_description";
        return ConnectionInfo.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withConnScope(connScope)
                .withName(name)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withDescription(description)
                .withCreateUser(user)
                .withUpdateUser(user)
                .build();
    }

    public static DataSourceRequest createDataSourceRequest(String name, Map<String, Object> datasourceConfigInfo, ConnectionConfigInfo commConnectionConfigInfo, DatasourceType type) {
        return DataSourceRequest.newBuilder()
                .withDatasourceType(type)
                .withName(name)
                .withDatasourceConfigInfo(datasourceConfigInfo)
                .withDatasourceConnection(createDatasourceConnection(commConnectionConfigInfo))
                .withCreateUser("createUser")
                .withUpdateUser("updateUser")
                .build();
    }

    public static Map<String, Object> createHostPortDatasourceConfig(String host, Integer port) {
        Map<String, Object> datasourceConfigInfo = new HashMap<>();
        datasourceConfigInfo.put("host", host);
        datasourceConfigInfo.put("port", port);
        return datasourceConfigInfo;
    }

    public static Map<String, Object> createAthenaDatasourceConfig(String url) {
        Map<String, Object> datasourceConfigInfo = new HashMap<>();
        datasourceConfigInfo.put("athenaUrl", url);
        return datasourceConfigInfo;
    }
}
