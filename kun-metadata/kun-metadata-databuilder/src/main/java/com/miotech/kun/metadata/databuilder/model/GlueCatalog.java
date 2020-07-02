package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GlueCatalog extends Catalog {

    private final String accessKey;

    private final String secretKey;

    private final String region;

    @JsonCreator
    public GlueCatalog(@JsonProperty("accessKey") String accessKey,
                       @JsonProperty("secretKey") String secretKey,
                       @JsonProperty("region") String region) {
        super(Type.GLUE);
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return "GlueCatalog{" +
                "accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", region='" + region + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String accessKey;
        private String secretKey;
        private String region;

        private Builder() {
        }

        public Builder withAccessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder withSecretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder withRegion(String region) {
            this.region = region;
            return this;
        }

        public GlueCatalog build() {
            return new GlueCatalog(accessKey, secretKey, region);
        }
    }
}
