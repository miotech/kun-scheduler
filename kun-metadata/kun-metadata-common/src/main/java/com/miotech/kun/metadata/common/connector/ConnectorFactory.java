package com.miotech.kun.metadata.common.connector;

import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.connection.JdbcConnectionInfo;

public class ConnectorFactory {

    public static Connector generateConnector(DataSource datasource) {
        ConnectionInfo connectionInfo = datasource.getConnectionConfig().getDataConnection();
        return generateConnector(connectionInfo);
    }

    public static Connector generateConnector(ConnectionInfo connectionInfo) {
        if (connectionInfo instanceof JdbcConnectionInfo) {
            return new JdbcConnector((JdbcConnectionInfo) connectionInfo);
        } else {
            throw new IllegalArgumentException(connectionInfo.getConnectionType() + " connector not support yet");
        }
    }
}
