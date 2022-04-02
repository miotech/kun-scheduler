package com.miotech.kun.dataquality.core.model;

import com.miotech.kun.metadata.core.model.dataset.Dataset;

public class DataQualityOperatorContext {

    private final Dataset dataset;

    public final String validatingVersion;

    public final ValidateResult validateResult;

    public DataQualityOperatorContext(Dataset dataset, String validatingVersion, ValidateResult validateResult) {
        this.dataset = dataset;
        this.validatingVersion = validatingVersion;
        this.validateResult = validateResult;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public String getValidatingVersion() {
        return validatingVersion;
    }

    public ValidateResult getValidateResult() {
        return validateResult;
    }

    public static DataQualityOperatorContextBuilder newBuilder() {
        return new DataQualityOperatorContextBuilder();
    }


    public static final class DataQualityOperatorContextBuilder {
        public String validatingVersion;
        public ValidateResult validateResult;
        private Dataset dataset;

        private DataQualityOperatorContextBuilder() {
        }

        public DataQualityOperatorContextBuilder withDataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public DataQualityOperatorContextBuilder withValidatingVersion(String validatingVersion) {
            this.validatingVersion = validatingVersion;
            return this;
        }

        public DataQualityOperatorContextBuilder withValidateResult(ValidateResult validateResult) {
            this.validateResult = validateResult;
            return this;
        }

        public DataQualityOperatorContext build() {
            return new DataQualityOperatorContext(dataset, validatingVersion, validateResult);
        }
    }
}
