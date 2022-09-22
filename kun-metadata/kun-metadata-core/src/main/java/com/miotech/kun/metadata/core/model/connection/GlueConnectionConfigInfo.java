package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class GlueConnectionConfigInfo extends ConnectionConfigInfo {
    private final String glueAccessKey;

    private final String glueSecretKey;

    private final String glueRegion;

    @JsonCreator
    public GlueConnectionConfigInfo(@JsonProperty("connectionType") ConnectionType connectionType,
                                    @JsonProperty("glueAccessKey") String glueAccessKey,
                                    @JsonProperty("glueSecretKey") String glueSecretKey,
                                    @JsonProperty("glueRegion") String glueRegion) {
        super(connectionType);
        this.glueAccessKey = glueAccessKey;
        this.glueSecretKey = glueSecretKey;
        this.glueRegion = glueRegion;
    }

    public String getGlueAccessKey() {
        return glueAccessKey;
    }

    public String getGlueSecretKey() {
        return glueSecretKey;
    }

    public String getGlueRegion() {
        return glueRegion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GlueConnectionConfigInfo that = (GlueConnectionConfigInfo) o;
        return Objects.equals(glueAccessKey, that.glueAccessKey) && Objects.equals(glueSecretKey, that.glueSecretKey) && Objects.equals(glueRegion, that.glueRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), glueAccessKey, glueSecretKey, glueRegion);
    }

    @Override
    public String toString() {
        return "GlueConnectionInfo{" +
                "glueAccessKey='" + glueAccessKey + '\'' +
                ", glueSecretKey='" + glueSecretKey + '\'' +
                ", glueRegion='" + glueRegion + '\'' +
                '}';
    }
}
