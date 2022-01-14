package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatasetBasicInfo {

    private Long gid;

    private String name;

    private String datasource;

    private String database;

    private String schema;

    private String description;

    private String type;

    private Watermark highWatermark;

    private Watermark lowWatermark;

    private List<String> owners;

    private List<String> tags;

    private Boolean deleted;

    public DatasetBasicInfo() {
    }

    @JsonCreator
    public DatasetBasicInfo(@JsonProperty("gid") Long gid,
                            @JsonProperty("name") String name,
                            @JsonProperty("datasource") String datasource,
                            @JsonProperty("database") String database,
                            @JsonProperty("schema") String schema,
                            @JsonProperty("description") String description,
                            @JsonProperty("type") String type,
                            @JsonProperty("highWatermark") Watermark highWatermark,
                            @JsonProperty("lowWatermark") Watermark lowWatermark,
                            @JsonProperty("owners") List<String> owners,
                            @JsonProperty("tags") List<String> tags,
                            @JsonProperty("deleted") Boolean deleted) {
        this.gid = gid;
        this.name = name;
        this.datasource = datasource;
        this.database = database;
        this.schema = schema;
        this.description = description;
        this.type = type;
        this.highWatermark = highWatermark;
        this.lowWatermark = lowWatermark;
        this.owners = owners;
        this.tags = tags;
        this.deleted = deleted;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Watermark getHighWatermark() {
        return highWatermark;
    }

    public void setHighWatermark(Watermark highWatermark) {
        this.highWatermark = highWatermark;
    }

    public Watermark getLowWatermark() {
        return lowWatermark;
    }

    public void setLowWatermark(Watermark lowWatermark) {
        this.lowWatermark = lowWatermark;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
