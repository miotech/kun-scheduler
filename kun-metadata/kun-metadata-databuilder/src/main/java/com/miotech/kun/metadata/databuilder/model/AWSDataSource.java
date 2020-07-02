package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AWSDataSource extends DataSource {

    private final String glueAccessKey;

    private final String glueSecretKey;

    private final String glueRegion;

    private final String athenaUrl;

    private final String athenaUsername;

    private final String athenaPassword;

    @JsonCreator
    public AWSDataSource(@JsonProperty("id") long id,
                         @JsonProperty("glueAccessKey") String glueAccessKey,
                         @JsonProperty("glueSecretKey") String glueSecretKey,
                         @JsonProperty("glueRegion") String glueRegion,
                         @JsonProperty("athenaUrl") String athenaUrl,
                         @JsonProperty("athenaUsername") String athenaUsername,
                         @JsonProperty("athenaPassword") String athenaPassword) {
        super(id, Type.AWS);
        this.glueAccessKey = glueAccessKey;
        this.glueSecretKey = glueSecretKey;
        this.glueRegion = glueRegion;
        this.athenaUrl = athenaUrl;
        this.athenaUsername = athenaUsername;
        this.athenaPassword = athenaPassword;
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

    public String getAthenaUrl() {
        return athenaUrl;
    }

    public String getAthenaUsername() {
        return athenaUsername;
    }

    public String getAthenaPassword() {
        return athenaPassword;
    }

    public static Builder clone(AWSDataSource awsDataSource) {
        return newBuilder().withId(awsDataSource.getId())
                .withGlueAccessKey(awsDataSource.getGlueAccessKey())
                .withGlueSecretKey(awsDataSource.getGlueSecretKey())
                .withGlueRegion(awsDataSource.getGlueRegion())
                .withAthenaUrl(awsDataSource.getAthenaUrl())
                .withAthenaUsername(awsDataSource.getAthenaUsername())
                .withAthenaPassword(awsDataSource.getAthenaPassword());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String glueAccessKey;
        private String glueSecretKey;
        private String glueRegion;
        private String athenaUrl;
        private String athenaUsername;
        private String athenaPassword;
        private long id;

        private Builder() {
        }

        public Builder withGlueAccessKey(String glueAccessKey) {
            this.glueAccessKey = glueAccessKey;
            return this;
        }

        public Builder withGlueSecretKey(String glueSecretKey) {
            this.glueSecretKey = glueSecretKey;
            return this;
        }

        public Builder withGlueRegion(String glueRegion) {
            this.glueRegion = glueRegion;
            return this;
        }

        public Builder withAthenaUrl(String athenaUrl) {
            this.athenaUrl = athenaUrl;
            return this;
        }

        public Builder withAthenaUsername(String athenaUsername) {
            this.athenaUsername = athenaUsername;
            return this;
        }

        public Builder withAthenaPassword(String athenaPassword) {
            this.athenaPassword = athenaPassword;
            return this;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }


        public AWSDataSource build() {
            return new AWSDataSource(id, glueAccessKey, glueSecretKey, glueRegion, athenaUrl, athenaUsername, athenaPassword);
        }
    }
}
