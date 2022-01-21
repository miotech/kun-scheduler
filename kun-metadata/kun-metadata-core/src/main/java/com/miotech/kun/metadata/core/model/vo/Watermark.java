package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Watermark {

    private Double time;

    public Watermark() {
    }

    @JsonCreator
    public Watermark(@JsonProperty("time") Double time) {
        this.time = time;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }
}
