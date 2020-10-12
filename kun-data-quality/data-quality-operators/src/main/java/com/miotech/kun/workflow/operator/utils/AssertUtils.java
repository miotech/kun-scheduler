package com.miotech.kun.workflow.operator.utils;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.operator.model.FieldType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: Jie Chen
 * @created: 2020/7/15
 */
public class AssertUtils {

    private static final String IS = "IS";

    private static final String IS_NOT = "IS_NOT";

    private static final String EQ = "=";

    private static final String NEQ = "!=";

    private static final String GT = ">";

    private static final String GTE = ">=";

    private static final String LT = "<";

    private static final String LTE = "<=";

    public static boolean doAssert(String expectedType,
                                   String operator,
                                   Object originalValue,
                                   String expectedValue) {
        if (originalValue == null) {
            return false;
        }
        if (FieldType.BOOLEAN.name().equalsIgnoreCase(expectedType)) {
            if (IS.equalsIgnoreCase(operator) || EQ.equals(operator)) {
                Boolean originBoolean;
                if (originalValue instanceof Boolean) {
                   originBoolean = (Boolean) originalValue;
                } else if (originalValue instanceof String) {
                    originBoolean = Boolean.valueOf((String) originalValue);
                } else {
                    throw newUnsupportedValue(originalValue, FieldType.BOOLEAN);
                }
                return originBoolean.toString().equalsIgnoreCase(expectedValue);
            }
            throw newUnsupportedOperator(operator, FieldType.BOOLEAN);
        } else if (FieldType.NUMBER.name().equalsIgnoreCase(expectedType)) {
            Number originNumber;
            if (!(originalValue instanceof Number)) {
                throw newUnsupportedValue(originalValue, FieldType.NUMBER);
            }
            originNumber = (Number) originalValue;
            return compareString(originNumber.toString(), expectedValue, operator, FieldType.NUMBER);
        } else if (FieldType.STRING.name().equalsIgnoreCase(expectedType)) {
            String originString;
            if (!(originalValue instanceof String)) {
                throw newUnsupportedValue(originalValue, FieldType.STRING);
            }
            originString = (String) originalValue;
            return compareString(originString, expectedValue, operator, FieldType.STRING);
        } else {
            throw ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported field type: " + expectedType));
        }
    }

    private static boolean compareString(String originString,
                                         String expectedValue,
                                         String operator,
                                         FieldType fieldType) {
        int compareResult = StringUtils.compare(originString, expectedValue);
        if (EQ.equals(operator)) {
            return compareResult == 0;
        } else if (NEQ.equals(operator)) {
            return compareResult != 0;
        } else if (GT.equals(operator)) {
            return compareResult > 0;
        } else if (GTE.equals(operator)) {
            return compareResult >= 0;
        } else if (LT.equals(operator)) {
            return compareResult < 0;
        } else if (LTE.equals(operator)) {
            return compareResult <= 0;
        } else {
            throw newUnsupportedOperator(operator, fieldType);
        }
    }

    private static RuntimeException newUnsupportedValue(Object value, FieldType fieldType) {
        return ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported value: " + value + " for type: " + fieldType.name()));
    }

    private static RuntimeException newUnsupportedOperator(String operator, FieldType fieldType) {
        return ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported operator: " + operator + " for type:" + fieldType.name()));
    }
}
