package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.constant.ResourceType;

import java.time.OffsetDateTime;

/**
 * @program: kun
 * @description: DataSet Resource Attribute
 * @author: zemin  huang
 * @create: 2022-03-08 10:11
 **/
public class DataSetResourceAttribute extends ResourceAttribute {
    private String datasourceName;
    private String databaseName;
    private String tags;
    private OffsetDateTime highWatermark;
    private String datasourceType;
    private String datasourceAttrs;
@JsonCreator
    public DataSetResourceAttribute(
        @JsonProperty("owners")   String owners,
        @JsonProperty("datasourceName")  String datasourceName,
        @JsonProperty("databaseName")  String databaseName,
        @JsonProperty("tags")  String tags,
        @JsonProperty("highWatermark") OffsetDateTime highWatermark,
        @JsonProperty("datasourceType") String datasourceType,
        @JsonProperty("datasourceAttrs") String datasourceAttrs) {
        super(owners);
        this.datasourceName = datasourceName;
        this.databaseName = databaseName;
        this.tags = tags;
        this.highWatermark = highWatermark;
        this.datasourceType = datasourceType;
        this.datasourceAttrs = datasourceAttrs;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTags() {
        return tags;
    }

    public OffsetDateTime getHighWatermark() {
        return highWatermark;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public String getDatasourceAttrs() {
        return datasourceAttrs;
    }


    public static final class Builder {
        private String datasourceName;
        private String databaseName;
        private String tags;
        private OffsetDateTime highWatermark;
        private String datasourceType;
        private String datasourceAttrs;
        private String owners;

        private Builder() {
        }

        public static Builder newBilder() {
            return new Builder();
        }

        public Builder withDatasourceName(String datasourceName) {
            this.datasourceName = datasourceName;
            return this;
        }

        public Builder withDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder withTags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder withHighWatermark(OffsetDateTime highWatermark) {
            this.highWatermark = highWatermark;
            return this;
        }

        public Builder withDatasourceType(String datasourceType) {
            this.datasourceType = datasourceType;
            return this;
        }

        public Builder withDatasourceAttrs(String datasourceAttrs) {
            this.datasourceAttrs = datasourceAttrs;
            return this;
        }


        public Builder withOwners(String owners) {
            this.owners = owners;
            return this;
        }

        public DataSetResourceAttribute build() {
            return new DataSetResourceAttribute(owners, datasourceName, databaseName, tags,
                    highWatermark, datasourceType, datasourceAttrs);
        }
    }
}
