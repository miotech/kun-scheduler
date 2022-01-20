package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetFieldInfo {

    private Long id;

    private String name;

    private String type;

    private Watermark highWatermark;

    private String description;

    private Long notNullCount;

    private Double notNullPercentage;

    private Long distinctCount;

    public DatasetFieldInfo() {
    }

    @JsonCreator
    public DatasetFieldInfo(@JsonProperty("id") Long id,
                            @JsonProperty("name") String name,
                            @JsonProperty("type") String type,
                            @JsonProperty("highWatermark") Watermark highWatermark,
                            @JsonProperty("description") String description,
                            @JsonProperty("notNullCount") Long notNullCount,
                            @JsonProperty("notNullPercentage") Double notNullPercentage,
                            @JsonProperty("distinctCount") Long distinctCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.highWatermark = highWatermark;
        this.description = description;
        this.notNullCount = notNullCount;
        this.notNullPercentage = notNullPercentage;
        this.distinctCount = distinctCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getNotNullCount() {
        return notNullCount;
    }

    public void setNotNullCount(Long notNullCount) {
        this.notNullCount = notNullCount;
    }

    public Double getNotNullPercentage() {
        return notNullPercentage;
    }

    public void setNotNullPercentage(Double notNullPercentage) {
        this.notNullPercentage = notNullPercentage;
    }

    public Long getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(Long distinctCount) {
        this.distinctCount = distinctCount;
    }
}
