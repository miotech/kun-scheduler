package com.miotech.kun.dataquality.core;

public class ExpectationFactory {

    private ExpectationFactory() {
    }

    public static Expectation get(ExpectationSpec spec) {
        if (spec.getMethod().getMode().equals(ExpectationMethod.Mode.JDBC)) {
            return new JDBCExpectation(spec);
        }

        throw new UnsupportedOperationException(String.format("Expectation mode: %s not supported", spec.getMethod().getMode()));
    }

}
