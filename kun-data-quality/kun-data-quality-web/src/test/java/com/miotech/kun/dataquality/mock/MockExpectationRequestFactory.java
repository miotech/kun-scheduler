package com.miotech.kun.dataquality.mock;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.web.model.bo.ExpectationRequest;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

public class MockExpectationRequestFactory {

    private MockExpectationRequestFactory() {
    }

    public static ExpectationRequest create() {
        long datasetGid = IdGenerator.getInstance().nextId();
        return ExpectationRequest.builder()
                .name("expectation_name")
                .types(ImmutableList.of("type_a", "type_b"))
                .description("desc")
                .granularity(Metrics.Granularity.CUSTOM.name())
                .templateName("CUSTOM_SQL")
                .payload(ImmutableMap.of("sql", "select count(1) c from test", "field", "c"))
                .datasetGid(datasetGid)
                .relatedDatasetGids(ImmutableList.of(datasetGid))
                .caseType(CaseType.SKIP)
                .build();
    }

    public static ExpectationRequest create(List<Long> relatedTableIds, Long primaryDatasetId) {
        return ExpectationRequest.builder()
                .name("expectation_name")
                .types(ImmutableList.of("type_a", "type_b"))
                .description("desc")
                .granularity(Metrics.Granularity.CUSTOM.name())
                .templateName("CUSTOM_SQL")
                .payload(ImmutableMap.of("sql", "select count(1) c from test", "field", "c"))
                .datasetGid(primaryDatasetId)
                .relatedDatasetGids(relatedTableIds)
                .caseType(CaseType.SKIP)
                .build();
    }

}
