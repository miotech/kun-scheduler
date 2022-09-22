package com.miotech.kun.metadata.common.factory;


import com.miotech.kun.metadata.common.utils.DatasourceDsiFormatter;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.datasource.DataSourceBasicInfo;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDataSourceFactory {

    private MockDataSourceFactory() {
    }


    public static DataSourceBasicInfo createDataSourceBasicInfo(long dataSourceId, String name, Map<String, Object> datasourceConfigInfo, DatasourceType type, List<String> tags, String createUser, String updateUser) {
        String dsi = DatasourceDsiFormatter.getDsi(type, datasourceConfigInfo);
        return DataSourceBasicInfo.newBuilder()
                .withId(dataSourceId)
                .withDatasourceType(type)
                .withName(name)
                .withDatasourceConfigInfo(datasourceConfigInfo)
                .withDsi(dsi)
                .withTags(tags)
                .withCreateUser(createUser)
                .withCreateTime(OffsetDateTime.now())
                .withUpdateUser(updateUser)
                .withUpdateTime(OffsetDateTime.now())
                .build();
    }


    public static DataSourceRequest createRequest(String name, Map<String, Object> datasourceConfigInfo, DatasourceConnection datasourceConnection, DatasourceType type, List<String> tags, String user) {
        return DataSourceRequest.newBuilder()
                .withName(name)
                .withDatasourceConnection(datasourceConnection)
                .withDatasourceConfigInfo(datasourceConfigInfo)
                .withDatasourceType(type)
                .withTags(tags)
                .withCreateUser(user)
                .withUpdateUser(user)
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
