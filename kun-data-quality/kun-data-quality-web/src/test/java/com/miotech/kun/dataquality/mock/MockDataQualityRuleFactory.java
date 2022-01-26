package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;

public class MockDataQualityRuleFactory {

    private MockDataQualityRuleFactory() {
    }

    public static DataQualityRule create(String field, String operator, String type, String expectedValue, String originalValue) {
        DataQualityRule dataQualityRule = new DataQualityRule();
        dataQualityRule.setField(field);
        dataQualityRule.setOperator(operator);
        dataQualityRule.setExpectedType(type);
        dataQualityRule.setExpectedValue(expectedValue);
        dataQualityRule.setOriginalValue(originalValue);
        return dataQualityRule;
    }

}
