package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class HiveMetaStoreConnectionConfigInfo extends ConnectionConfigInfo {

    private final String metaStoreUris;

    @JsonCreator
    public HiveMetaStoreConnectionConfigInfo(@JsonProperty("connectionType") ConnectionType connectionType,
                                             @JsonProperty("metaStoreUris") String metaStoreUris) {
        super(connectionType);
        this.metaStoreUris = metaStoreUris;
    }

    public HiveMetaStoreConnectionConfigInfo(ConnectionType connectionType) {
        this(connectionType, null);
    }

    public String getMetaStoreUris() {
        return metaStoreUris;
    }

    @Override
    public boolean sameDatasource(Object o) {
        return super.sameDatasource(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HiveMetaStoreConnectionConfigInfo that = (HiveMetaStoreConnectionConfigInfo) o;
        return Objects.equals(metaStoreUris, that.metaStoreUris);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), metaStoreUris);
    }

    @Override
    public String toString() {
        return "HiveMetaStoreConnectionInfo{" +
                "metaStoreUris='" + metaStoreUris + '\'' +
                '}';
    }
}
