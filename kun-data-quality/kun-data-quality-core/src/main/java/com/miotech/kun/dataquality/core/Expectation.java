package com.miotech.kun.dataquality.core;

public interface Expectation {

    ExpectationSpec getSpec();

    ValidationResult validate();

}
