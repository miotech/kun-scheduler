package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = VariableUpsertVO.Builder.class)
public class VariableUpsertVO {
    private final String key;

    private final String value;

    private final Boolean encrypted;

    private VariableUpsertVO(String key, String value, Boolean encrypted) {
        this.key = key;
        this.value = value;
        this.encrypted = encrypted != null && encrypted;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Boolean isEncrypted() {
        return encrypted;
    }

    public static Builder newBuilder() { return new Builder(); }

    @JsonPOJOBuilder
    public static final class Builder {
        private String key;
        private String value;
        private Boolean encrypted;

        private Builder() {
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withEncrypted(Boolean encrypted) {
            this.encrypted = encrypted;
            return this;
        }

        public VariableUpsertVO build() {
            return new VariableUpsertVO(key, value, encrypted);
        }
    }
}
