package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.constant.HiveAnalysisEngine;
import com.miotech.kun.metadata.constant.MetaStoreType;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;

public class HiveCluster extends Cluster {

    private HiveAnalysisEngine hiveAnalysisEngine;

    private final String dataStoreUrl;

    private final String dataStoreUsername;

    private final String dataStorePassword;

    private MetaStoreType metaStoreType;

    private final String metaStoreUrl;

    private final String metaStoreUsername;

    private final String metaStorePassword;

    public HiveAnalysisEngine getHiveAnalysisEngine() {
        return hiveAnalysisEngine;
    }

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getDataStoreUsername() {
        return dataStoreUsername;
    }

    public String getDataStorePassword() {
        return dataStorePassword;
    }

    public MetaStoreType getMetaStoreType() {
        return metaStoreType;
    }

    public String getMetaStoreUrl() {
        return metaStoreUrl;
    }

    public String getMetaStoreUsername() {
        return metaStoreUsername;
    }

    public String getMetaStorePassword() {
        return metaStorePassword;
    }

    private void setHiveAnalysisEngine(String dataStoreUrl) {
        Preconditions.checkNotNull(dataStoreUrl, "dataStoreUrl should not be null");
        String[] infos = dataStoreUrl.split(":");
        if (infos.length <= 1) {
            throw new RuntimeException("invalid dataStoreUrl: " + dataStoreUrl);
        }

        switch (infos[1]) {
            case "hive2":
                this.hiveAnalysisEngine = HiveAnalysisEngine.HIVE;
                break;
            case "presto":
                this.hiveAnalysisEngine = HiveAnalysisEngine.PRESTO;
                break;
            case "awsathena":
                this.hiveAnalysisEngine = HiveAnalysisEngine.ATHENA;
                break;
            default:
                throw new RuntimeException("Unsupported data source type");
        }
    }

    private void setMetaStoreType(String metaStoreUrl) {
        Preconditions.checkNotNull(metaStoreUrl, "metaStoreUrl should not be null");
        if (metaStoreUrl.startsWith("jdbc")) {
            this.metaStoreType = MetaStoreType.MYSQL;
        } else {
            this.metaStoreType = MetaStoreType.GLUE;
        }

    }

    public static DatabaseType convertFromAnalysisEngine(HiveAnalysisEngine hiveAnalysisEngine) {
        switch (hiveAnalysisEngine) {
            case HIVE:
                return DatabaseType.HIVE;
            case PRESTO:
                return DatabaseType.PRESTO;
            case ATHENA:
                return DatabaseType.ATHENA;
            default:
                throw new RuntimeException("Invalid HiveAnalysisEngine: " + hiveAnalysisEngine);
        }
    }

    @JsonCreator
    public HiveCluster(@JsonProperty("clusterId") long clusterId,
                       @JsonProperty("dataStoreUrl") String dataStoreUrl,
                       @JsonProperty("dataStoreUsername") String dataStoreUsername,
                       @JsonProperty("dataStorePassword") String dataStorePassword,
                       @JsonProperty("metaStoreUrl") String metaStoreUrl,
                       @JsonProperty("metaStoreUsername") String metaStoreUsername,
                       @JsonProperty("metaStorePassword") String metaStorePassword) {
        super(clusterId);
        setHiveAnalysisEngine(dataStoreUrl);
        setMetaStoreType(metaStoreUrl);
        this.dataStoreUrl = dataStoreUrl;
        this.dataStoreUsername = dataStoreUsername;
        this.dataStorePassword = dataStorePassword;
        this.metaStoreUrl = metaStoreUrl;
        this.metaStoreUsername = metaStoreUsername;
        this.metaStorePassword = metaStorePassword;
    }

    public static HiveCluster.Builder newBuilder() {
        return new HiveCluster.Builder();
    }

    @Override
    public String toString() {
        return "HiveCluster{" +
                "hiveAnalysisEngine=" + hiveAnalysisEngine +
                ", dataStoreUrl='" + dataStoreUrl + '\'' +
                ", dataStoreUsername='" + dataStoreUsername + '\'' +
                ", dataStorePassword='" + dataStorePassword + '\'' +
                ", metaStoreType=" + metaStoreType +
                ", metaStoreUrl='" + metaStoreUrl + '\'' +
                ", metaStoreUsername='" + metaStoreUsername + '\'' +
                ", metaStorePassword='" + metaStorePassword + '\'' +
                '}';
    }

    public static final class Builder {
        private long clusterId;
        private String dataStoreUrl;
        private String dataStoreUsername;
        private String dataStorePassword;
        private String metaStoreUrl;
        private String metaStoreUsername;
        private String metaStorePassword;

        private Builder() {
        }

        public Builder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public Builder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public Builder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public Builder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public Builder withMetaStoreUrl(String metaStoreUrl) {
            this.metaStoreUrl = metaStoreUrl;
            return this;
        }

        public Builder withMetaStoreUsername(String metaStoreUsername) {
            this.metaStoreUsername = metaStoreUsername;
            return this;
        }

        public Builder withMetaStorePassword(String metaStorePassword) {
            this.metaStorePassword = metaStorePassword;
            return this;
        }

        public HiveCluster build() {
            return new HiveCluster(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword, metaStoreUrl,
                    metaStoreUsername, metaStorePassword);
        }
    }
}
