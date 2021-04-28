package com.miotech.kun.dataplatform.model.tasktemplate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = Option.Builder.class)
public class Option {

    final String value;

    final String label;

    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        String value;
        String label;

        private Builder() {
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Option build() {
            return new Option(value, label);
        }
    }
}