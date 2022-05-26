package com.miotech.kun.workflow.worker.datasource;

public class RpcConnectionConfig {

    private final String connectionId;
    private final boolean autoCommit;

    public RpcConnectionConfig(String connectionId, boolean autoCommit) {
        this.connectionId = connectionId;
        this.autoCommit = autoCommit;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public static RpcConnectionConfigBuilder newBuilder(){
        return new RpcConnectionConfigBuilder();
    }

    @Override
    public String toString() {
        return "RpcConnectionConfig{" +
                "connectionId='" + connectionId + '\'' +
                ", autoCommit=" + autoCommit +
                '}';
    }


    public static final class RpcConnectionConfigBuilder {
        private String connectionId;
        private boolean autoCommit;

        private RpcConnectionConfigBuilder() {
        }

        public RpcConnectionConfigBuilder withConnectionId(String connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public RpcConnectionConfigBuilder withAutoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
            return this;
        }

        public RpcConnectionConfig build() {
            return new RpcConnectionConfig(connectionId, autoCommit);
        }
    }
}
