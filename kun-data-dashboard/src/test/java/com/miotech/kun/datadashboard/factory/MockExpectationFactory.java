package com.miotech.kun.datadashboard.factory;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.core.metrics.Metrics;

public class MockExpectationFactory {

    private MockExpectationFactory() {
    }

    public static Expectation create(Long datasetGid) {
        Dataset dataset = Dataset.builder().gid(datasetGid).dataSource(null).build();
        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName("Expectation Name")
                .withDescription("Expectation Desc")
                .withGranularity(Metrics.Granularity.CUSTOM.name())
                .withTemplate(ExpectationTemplate.newBuilder().withName("CUSTOM_SQL").build())
                .withTrigger(Expectation.ExpectationTrigger.SCHEDULED)
                .withDataset(dataset)
                .withTaskId(IdGenerator.getInstance().nextId())
                .withCaseType(CaseType.SKIP)
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withCreateUser("admin")
                .withUpdateUser("admin")
                .build();
    }

    public static Expectation createWithTaskId(Long taskId) {
        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName("Expectation Name")
                .withDescription("Expectation Desc")
                .withTrigger(Expectation.ExpectationTrigger.SCHEDULED)
                .withDataset(Dataset.builder().gid(IdGenerator.getInstance().nextId()).dataSource(null).build())
                .withTaskId(taskId)
                .withCaseType(CaseType.BLOCK)
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withCreateUser("admin")
                .withUpdateUser("admin")
                .build();
    }

    public static Expectation create() {
        return create(IdGenerator.getInstance().nextId());
    }

}
