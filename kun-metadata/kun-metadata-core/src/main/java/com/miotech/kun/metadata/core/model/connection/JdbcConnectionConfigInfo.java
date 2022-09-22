package com.miotech.kun.metadata.core.model.connection;

public abstract class JdbcConnectionConfigInfo extends ConnectionConfigInfo {
    public JdbcConnectionConfigInfo(ConnectionType connectionType) {
        super(connectionType);
    }

    @Override
    public boolean sameDatasource(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return this.getJdbcUrl().equals(((JdbcConnectionConfigInfo) o).getJdbcUrl());
    }

    public abstract String getJdbcUrl();

    public abstract String getUsername();

    public abstract String getPassword();
}
