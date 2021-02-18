package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = VariableVO.Builder.class)
public class VariableVO {

    private final String namespace;

    private final String key;

    private final String value;

    private final Boolean encrypted;

    private VariableVO(String namespace, String key, String value, Boolean encrypted) {
        this.namespace = namespace;
        this.key = key;
        this.value = value;
        this.encrypted = encrypted == null ? false: encrypted;
    }

    public String getNamespace() {
        return namespace;
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
        private String namespace;
        private String key;
        private String value;
        private Boolean encrypted;

        private Builder() {
        }

        public Builder withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
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

        public VariableVO build() {
            return new VariableVO(namespace, key, value, encrypted);
        }
    }
}
