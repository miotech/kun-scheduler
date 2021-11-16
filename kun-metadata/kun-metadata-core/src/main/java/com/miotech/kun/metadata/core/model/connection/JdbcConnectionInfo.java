package com.miotech.kun.metadata.core.model.connection;

public abstract class JdbcConnectionInfo extends ConnectionInfo{
    public JdbcConnectionInfo(ConnectionType connectionType) {
        super(connectionType);
    }

    public abstract String getJdbcUrl();
    public abstract String getUsername();
    public abstract String getPassword();
}
