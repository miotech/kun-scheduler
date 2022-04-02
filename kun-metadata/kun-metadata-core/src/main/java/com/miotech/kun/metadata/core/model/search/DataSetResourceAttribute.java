package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @program: kun
 * @description: DataSet Resource Attribute
 * @author: zemin  huang
 * @create: 2022-03-08 10:11
 **/
public class DataSetResourceAttribute extends ResourceAttribute {
    private String datasource;

    private String database;

    private String schema;

    private String type;

    private String tags;

    public String getDatasource() {
        return datasource;
    }

    public String getDatabase() {
        return database;
    }

    public String getSchema() {
        return schema;
    }

    public String getType() {
        return type;
    }


    public String getTags() {
        return tags;
    }

    @JsonCreator
    public DataSetResourceAttribute(
            @JsonProperty("datasource") String datasource,
            @JsonProperty("database") String database,
            @JsonProperty("schema") String schema,
            @JsonProperty("type") String type,
            @JsonProperty("owners") String owners,
            @JsonProperty("tags") String tags) {
        super(owners);
        this.datasource = datasource;
        this.database = database;
        this.schema = schema;
        this.type = type;
        this.tags = tags;
    }


    public static final class Builder {
        private String datasource;
        private String database;
        private String schema;
        private String type;
        private String owners;
        private String tags;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withDatasource(String datasource) {
            this.datasource = datasource;
            return this;
        }

        public Builder withDatabase(String database) {
            this.database = database;
            return this;
        }

        public Builder withSchema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }


        public Builder withTags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder withOwners(String owners) {
            this.owners = owners;
            return this;
        }

        public DataSetResourceAttribute build() {
            return new DataSetResourceAttribute(datasource, database, schema, type, owners, tags);
        }
    }
}
