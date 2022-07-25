package com.miotech.kun.dataquality.core.expectation;

public class ExpectationTemplate {

    private final String name;

    private final String granularity;

    private final String description;

    private final String converter;

    private final String displayParameters;

    public ExpectationTemplate(String name, String granularity, String description, String converter, String displayParameters) {
        this.name = name;
        this.granularity = granularity;
        this.description = description;
        this.converter = converter;
        this.displayParameters = displayParameters;
    }

    public String getName() {
        return name;
    }

    public String getGranularity() {
        return granularity;
    }

    public String getDescription() {
        return description;
    }

    public String getConverter() {
        return converter;
    }

    public String getDisplayParameters() {
        return displayParameters;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private String granularity;
        private String description;
        private String converter;
        private String displayParameters;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withGranularity(String granularity) {
            this.granularity = granularity;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withConverter(String converter) {
            this.converter = converter;
            return this;
        }

        public Builder withDisplayParameters(String displayParameters) {
            this.displayParameters = displayParameters;
            return this;
        }

        public ExpectationTemplate build() {
            return new ExpectationTemplate(name, granularity, description, converter, displayParameters);
        }
    }
}
