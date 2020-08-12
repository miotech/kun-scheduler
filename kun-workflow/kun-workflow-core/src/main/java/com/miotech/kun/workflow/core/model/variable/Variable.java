package com.miotech.kun.workflow.core.model.variable;


public class Variable {

    private final String namespace;

    private final String key;

    private final String value;

    private final Boolean encrypted;

    private Variable(String id, String key, String value, Boolean encrypted) {
        this.namespace = id;
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

    public String getFullKey() {
        return String.join(".", namespace, key);
    }

    public String getValue() {
        return value;
    }

    public Boolean isEncrypted() {
        return encrypted;
    }

    public Builder cloneBuilder() { return new Builder()
            .withNamespace(namespace)
            .withKey(key)
            .withValue(value)
            .withEncrypted(encrypted);
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private String namespace;
        private String key;
        private String value;
        private Boolean encrypted;

        private Builder() {
        }

        public Builder withNamespace(String id) {
            this.namespace = id;
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

        public Variable build() {
            return new Variable(namespace, key, value, encrypted);
        }
    }
}
