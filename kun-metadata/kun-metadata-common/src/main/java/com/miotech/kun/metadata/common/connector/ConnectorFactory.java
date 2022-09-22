package com.miotech.kun.metadata.common.connector;

import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.connection.JdbcConnectionConfigInfo;

public class ConnectorFactory {

    public static Connector generateConnector(DataSource datasource) {
        ConnectionConfigInfo connectionConfigInfo = datasource.getDatasourceConnection().getDataConnection().getConnectionConfigInfo();
        return generateConnector(connectionConfigInfo);
    }

    public static Connector generateConnector(ConnectionConfigInfo connectionConfigInfo) {
        if (connectionConfigInfo instanceof JdbcConnectionConfigInfo) {
            return new JdbcConnector((JdbcConnectionConfigInfo) connectionConfigInfo);
        } else {
            throw new IllegalArgumentException(connectionConfigInfo.getConnectionType() + " connector not support yet");
        }
    }
}
