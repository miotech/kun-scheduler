package com.miotech.kun.dataquality.core.converter;

import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.metrics.Metrics;

import java.util.Map;

public interface ExpectationConverter {

    Metrics convertMetrics(Map<String, Object> payload);

    Assertion convertAssertion(Map<String, Object> payload);

}
