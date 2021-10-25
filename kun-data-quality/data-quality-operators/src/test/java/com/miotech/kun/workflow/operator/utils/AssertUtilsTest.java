package com.miotech.kun.workflow.operator.utils;

import com.miotech.kun.workflow.operator.model.FieldType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class AssertUtilsTest {


    private static final String EQ = "=";

    private static final String NEQ = "!=";

    private static final String GT = ">";

    private static final String GTE = ">=";

    private static final String LT = "<";

    private static final String LTE = "<=";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @DataPoints("intValue")
    public static Integer[] intValues() {
        Integer[] originalValues = {3, 5, 8888, 11, 0, -2};
        return originalValues;
    }

    @DataPoints("longValue")
    public static Long[] longValues() {
        Long[] originalValues = {3l, 5l, 8575683534l, 11l, 0l, -2l};
        return originalValues;
    }

    @DataPoints("doubleValue")
    public static Double[] doubleValues() {
        Double[] originalValues = {3.5, 5.244, 0.35, -0.45};
        return originalValues;
    }

    @DataPoints("expectedValue")
    public static String[] expectedValues() {
        String[] originalValues = {"10", "6", "-5", "3"};
        return originalValues;
    }


    @Theory
    public void assertIntegerEQ(@FromDataPoints("intValue") Integer originalValue,
                                @FromDataPoints("expectedValue") String expectedValue) {
        Integer expectedInt = Integer.valueOf(expectedValue);
        boolean expectResult = originalValue.equals(expectedInt);

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), EQ, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertIntegerNEQ(@FromDataPoints("intValue") Integer originalValue,
                                 @FromDataPoints("expectedValue") String expectedValue) {
        Integer expectedInt = Integer.valueOf(expectedValue);
        boolean expectResult = !originalValue.equals(expectedInt);

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), NEQ, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertIntegerLET(@FromDataPoints("intValue") Integer originalValue,
                                 @FromDataPoints("expectedValue") String expectedValue) {
        Integer expectedInt = Integer.valueOf(expectedValue);
        boolean expectResult = originalValue <= expectedInt;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), LTE, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertIntegerLT(@FromDataPoints("intValue") Integer originalValue,
                                @FromDataPoints("expectedValue") String expectedValue) {
        Integer expectedInt = Integer.valueOf(expectedValue);
        boolean expectResult = originalValue < expectedInt;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), LT, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertIntegerGTE(@FromDataPoints("intValue") Integer originalValue,
                                 @FromDataPoints("expectedValue") String expectedValue) {
        Integer expectedInt = Integer.valueOf(expectedValue);
        boolean expectResult = originalValue >= expectedInt;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), GTE, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertIntegerGT(@FromDataPoints("intValue") Integer originalValue,
                                @FromDataPoints("expectedValue") String expectedValue) {
        Integer expectedInt = Integer.valueOf(expectedValue);
        boolean expectResult = originalValue > expectedInt;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), GT, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertLongEQ(@FromDataPoints("longValue") Long originalValue,
                                @FromDataPoints("expectedValue") String expectedValue) {
        Long expectedLong = Long.valueOf(expectedValue);
        boolean expectResult = originalValue.equals(expectedLong);

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), EQ, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertLongNEQ(@FromDataPoints("longValue") Long originalValue,
                              @FromDataPoints("expectedValue") String expectedValue) {
        Long expectedLong = Long.valueOf(expectedValue);
        boolean expectResult = !originalValue.equals(expectedLong);

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), NEQ, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertLongLET(@FromDataPoints("longValue") Long originalValue,
                              @FromDataPoints("expectedValue") String expectedValue) {
        Long expectedLong = Long.valueOf(expectedValue);
        boolean expectResult = originalValue <= expectedLong;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), LTE, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertLongLT(@FromDataPoints("longValue") Long originalValue,
                             @FromDataPoints("expectedValue") String expectedValue) {
        Long expectedLong = Long.valueOf(expectedValue);
        boolean expectResult = originalValue < expectedLong;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), LT, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertLongGTE(@FromDataPoints("longValue") Long originalValue,
                              @FromDataPoints("expectedValue") String expectedValue) {
        Long expectedLong = Long.valueOf(expectedValue);
        boolean expectResult = originalValue >= expectedLong;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), GTE, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertLongGT(@FromDataPoints("longValue") Long originalValue,
                             @FromDataPoints("expectedValue") String expectedValue) {
        Long expectedLong = Long.valueOf(expectedValue);
        boolean expectResult = originalValue > expectedLong;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), GT, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertDoubleGT(@FromDataPoints("doubleValue") Double originalValue,
                             @FromDataPoints("expectedValue") String expectedValue) {
        Double expectedDouble = Double.valueOf(expectedValue);
        boolean expectResult = originalValue > expectedDouble;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), GT, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertDoubleLT(@FromDataPoints("doubleValue") Double originalValue,
                               @FromDataPoints("expectedValue") String expectedValue) {
        Double expectedDouble = Double.valueOf(expectedValue);
        boolean expectResult = originalValue < expectedDouble;

        boolean assertResult = AssertUtils.doAssert(FieldType.NUMBER.name(), LT, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Test
    public void assertNotSupportOperator_should_throw_RuntimeException() throws RuntimeException {
        //prepare
        String expectedValue = "10";
        Integer originalValue = 8;

        // 2. Validate
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Unsupported operator: op for type:" + FieldType.NUMBER.name());

        AssertUtils.doAssert(FieldType.NUMBER.name(), "op", originalValue, expectedValue);

    }

    @Theory
    public void assertStringEQ(@FromDataPoints("expectedValue") String expectedValue) {
        String originalValue = "10";
        boolean expectResult = originalValue.equals(expectedValue);

        boolean assertResult = AssertUtils.doAssert(FieldType.STRING.name(), EQ, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

    @Theory
    public void assertStringNEQ(@FromDataPoints("expectedValue") String expectedValue) {
        String originalValue = "10";
        boolean expectResult = !originalValue.equals(expectedValue);

        boolean assertResult = AssertUtils.doAssert(FieldType.STRING.name(), NEQ, originalValue, expectedValue);

        assertThat(assertResult, is(expectResult));

    }

}
