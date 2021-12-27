package com.miotech.kun.dataquality.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.DateTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JDBCExpectationMethod extends ExpectationMethod {

    private final String sql;

    private final List<JDBCExpectationAssertion> assertions;

    @JsonCreator
    public JDBCExpectationMethod(@JsonProperty("sql") String sql,
                                 @JsonProperty("assertions") List<JDBCExpectationAssertion> assertions) {
        super(Mode.JDBC);
        this.sql = sql;
        this.assertions = assertions;
    }

    public String getSql() {
        return sql;
    }

    public List<JDBCExpectationAssertion> getAssertions() {
        return assertions;
    }

    @Override
    public ValidationResult validate(ResultSet rs) {
        ValidationResult.Builder vr = ValidationResult.newBuilder();
        try {
            if (rs.next()) {
                boolean passed = true;
                List<JDBCExpectationAssertionResult> assertionResults = Lists.newArrayList();
                for (JDBCExpectationAssertion assertion : assertions) {
                    String field = assertion.getField();
                    String originalValue = rs.getString(field);
                    boolean result = assertion.doAssert(originalValue);
                    if (!result) {
                        passed = false;
                    }

                    JDBCExpectationAssertionResult assertionResult = JDBCExpectationAssertionResult.from(assertion, originalValue);
                    assertionResults.add(assertionResult);
                }

                vr.withPassed(passed).withAssertionResults(assertionResults).withUpdateTime(DateTimeUtils.now());
            } else {
                vr.withPassed(false).withExecutionResult("No data returned").withUpdateTime(DateTimeUtils.now());
            }


        } catch (SQLException sqlException) {
            vr.withPassed(false).withExecutionResult(sqlException.getMessage()).withUpdateTime(DateTimeUtils.now());
        }

        return vr.build();
    }

}
