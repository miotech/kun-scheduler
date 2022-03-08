package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.bo.AssertionRequest;
import com.miotech.kun.dataquality.web.model.bo.ExpectationRequest;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

public class MockExpectationRequestFactory {

    private MockExpectationRequestFactory() {
    }

    public static ExpectationRequest create() {
        long datasetGid = IdGenerator.getInstance().nextId();
        MetricsRequest metricsRequest = MockMetricsRequestFactory.create("select count(1) c from test ", "c", datasetGid);
        AssertionRequest assertionRequest = MockAssertionRequestFactory.create();
        return ExpectationRequest.builder()
                .name("expectation_name")
                .types(ImmutableList.of("type_a", "type_b"))
                .description("desc")
                .metrics(metricsRequest)
                .assertion(assertionRequest)
                .relatedDatasetGids(ImmutableList.of(datasetGid))
                .isBlocking(false)
                .build();
    }

    public static ExpectationRequest create(List<Long> relatedTableIds, Long primaryDatasetId) {
        MetricsRequest metricsRequest = MockMetricsRequestFactory.create("select count(1) c from test ", "c", primaryDatasetId);
        AssertionRequest assertionRequest = MockAssertionRequestFactory.create();
        return ExpectationRequest.builder()
                .name("expectation_name")
                .types(ImmutableList.of("type_a", "type_b"))
                .description("desc")
                .metrics(metricsRequest)
                .assertion(assertionRequest)
                .relatedDatasetGids(relatedTableIds)
                .isBlocking(false)
                .build();
    }

}
