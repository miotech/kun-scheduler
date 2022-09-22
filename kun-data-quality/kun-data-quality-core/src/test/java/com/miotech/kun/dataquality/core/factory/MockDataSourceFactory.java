package com.miotech.kun.dataquality.core.factory;


import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class MockDataSourceFactory {

    private MockDataSourceFactory() {
    }


    public static DataSource createDataSource(long dataSourceId, String name, Map<String, Object> datasourceConfigInfo, DatasourceConnection datasourceConnection, DatasourceType type) {
        return DataSource.newBuilder()
                .withId(dataSourceId)
                .withDatasourceType(type)
                .withName(name)
                .withDatasourceConfigInfo(datasourceConfigInfo)
                .withDatasourceConnection(datasourceConnection)
                .withCreateTime(OffsetDateTime.now())
                .withUpdateTime(OffsetDateTime.now())
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
