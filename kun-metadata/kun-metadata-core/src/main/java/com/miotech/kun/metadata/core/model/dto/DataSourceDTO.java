package com.miotech.kun.metadata.core.model.dto;

import java.io.Serializable;

public class DataSourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    private final String type;

    private final Long typeId;

    private final DataSourceConnectionDTO connectionInfo;

    public DataSourceDTO(Long id, String type, Long typeId, DataSourceConnectionDTO connectionInfo) {
        this.id = id;
        this.type = type;
        this.typeId = typeId;
        this.connectionInfo = connectionInfo;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Long getTypeId() {
        return typeId;
    }

    public DataSourceConnectionDTO getConnectionInfo() {
        return connectionInfo;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String type;
        private Long typeId;
        private DataSourceConnectionDTO connectionInfo;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withTypeId(Long typeId) {
            this.typeId = typeId;
            return this;
        }

        public Builder withConnectionInfo(DataSourceConnectionDTO connectionInfo) {
            this.connectionInfo = connectionInfo;
            return this;
        }

        public DataSourceDTO build() {
            return new DataSourceDTO(id, type, typeId, connectionInfo);
        }
    }
}
