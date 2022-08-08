package com.miotech.kun.dataquality.core.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.assertion.EqualsAssertion;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public class PrimaryKeyExpectationConverterTest {

    @Test
    public void testConvertMetrics() {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("fields", ImmutableList.of("name", "age"));
        payload.put("databaseName", "dev");
        payload.put("tableName", "demo");

        PrimaryKeyExpectationConverter converter = new PrimaryKeyExpectationConverter();
        Metrics metrics = converter.convertMetrics(payload);

        assertThat(metrics, isA(SQLMetrics.class));
        SQLMetrics sqlMetrics = (SQLMetrics) metrics;
        assertThat(sqlMetrics.getSql(), is("select sum(case when t.c > 1 then 1 else 0 end) c from (select count(*) c from dev.demo group by name,age) t"));
        assertThat(sqlMetrics.getField(), is("c"));
    }

    @Test
    public void testConvertAssertion() {
        Map<String, Object> payload = Maps.newHashMap();

        PrimaryKeyExpectationConverter converter = new PrimaryKeyExpectationConverter();
        Assertion assertion = converter.convertAssertion(payload);

        assertThat(assertion, isA(EqualsAssertion.class));
        EqualsAssertion equalsAssertion = (EqualsAssertion) assertion;
        assertThat(equalsAssertion.getExpectedValue(), is("0"));
    }

}
