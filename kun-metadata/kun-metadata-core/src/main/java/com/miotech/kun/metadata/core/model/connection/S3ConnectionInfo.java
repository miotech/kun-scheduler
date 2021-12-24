package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class S3ConnectionInfo extends ConnectionInfo{

    private final String s3AccessKey;

    private final String s3SecretKey;

    private final String s3Region;

    @JsonCreator
    public S3ConnectionInfo(@JsonProperty("connectionType") ConnectionType connectionType,
                            @JsonProperty("s3AccessKey") String s3AccessKey,
                            @JsonProperty("s3SecretKey") String s3SecretKey,
                            @JsonProperty("s3Region") String s3Region) {
        super(connectionType);
        this.s3AccessKey = s3AccessKey;
        this.s3SecretKey = s3SecretKey;
        this.s3Region = s3Region;
    }

    public String getS3AccessKey() {
        return s3AccessKey;
    }

    public String getS3SecretKey() {
        return s3SecretKey;
    }

    public String getS3Region() {
        return s3Region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        S3ConnectionInfo that = (S3ConnectionInfo) o;
        return Objects.equals(s3AccessKey, that.s3AccessKey) && Objects.equals(s3SecretKey, that.s3SecretKey) && Objects.equals(s3Region, that.s3Region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), s3AccessKey, s3SecretKey, s3Region);
    }

    @Override
    public String toString() {
        return "S3ConnectionInfo{" +
                "s3AccessKey='" + s3AccessKey + '\'' +
                ", s3SecretKey='" + s3SecretKey + '\'' +
                ", s3Region='" + s3Region + '\'' +
                '}';
    }
}
