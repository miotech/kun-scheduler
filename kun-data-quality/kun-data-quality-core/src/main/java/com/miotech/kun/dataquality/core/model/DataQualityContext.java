package com.miotech.kun.dataquality.core.model;

import com.miotech.kun.metadata.core.model.dataset.Dataset;

public class DataQualityContext {

    private final Dataset dataset;

    public final String validatingVersion;

    public final ValidateResult validateResult;

    public DataQualityContext(Dataset dataset, String validatingVersion, ValidateResult validateResult) {
        this.dataset = dataset;
        this.validatingVersion = validatingVersion;
        this.validateResult = validateResult;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public static DataQualityContextBuilder newBuilder(){
        return new DataQualityContextBuilder();
    }

    public String getValidatingVersion() {
        return validatingVersion;
    }

    public ValidateResult getValidateResult() {
        return validateResult;
    }

    @Override
    public String toString() {
        return "DataQualityContext{" +
                "dataset=" + dataset +
                ", validatingVersion='" + validatingVersion + '\'' +
                ", validateResult=" + validateResult +
                '}';
    }

    public static final class DataQualityContextBuilder {
        public String validatingVersion;
        public ValidateResult validateResult;
        private Dataset dataset;

        private DataQualityContextBuilder() {
        }


        public DataQualityContextBuilder withDataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public DataQualityContextBuilder withValidatingVersion(String validatingVersion) {
            this.validatingVersion = validatingVersion;
            return this;
        }

        public DataQualityContextBuilder withValidateResult(ValidateResult validateResult) {
            this.validateResult = validateResult;
            return this;
        }

        public DataQualityContext build() {
            return new DataQualityContext(dataset, validatingVersion, validateResult);
        }
    }
}
