package com.miotech.kun.dataquality.mock;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.assertion.EqualsAssertion;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.JDBCExpectationAssertion;
import com.miotech.kun.dataquality.core.expectation.JDBCExpectationMethod;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import com.miotech.kun.dataquality.core.expectation.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

public class MockExpectationFactory {

    private MockExpectationFactory() {
    }

    public static Expectation create(Long datasetGid) {
        Dataset dataset = Dataset.builder().gid(datasetGid).dataSource(DataSource.newBuilder().withId(IdGenerator.getInstance().nextId()).build()).build();
        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName("Expectation Name")
                .withDescription("Expectation Desc")
                .withMethod(new JDBCExpectationMethod("select count(1) c from demo",
                        ImmutableList.of(new JDBCExpectationAssertion("c", JDBCExpectationAssertion.ComparisonOperator.EQUALS, "=", "NUMBER", "0"))))
                .withMetrics(new SQLMetrics("sql metrics", "desc", Metrics.Granularity.CUSTOM, dataset, "select count(1) c from demo", "c"))
                .withAssertion(new EqualsAssertion("=", "0"))
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
                .withMethod(new JDBCExpectationMethod("select count(1) c from demo",
                        ImmutableList.of(new JDBCExpectationAssertion("c", JDBCExpectationAssertion.ComparisonOperator.EQUALS, "=", "NUMBER", "0"))))
                .withTrigger(Expectation.ExpectationTrigger.SCHEDULED)
                .withDataset(Dataset.builder().gid(IdGenerator.getInstance().nextId()).dataSource(DataSource.newBuilder().withId(IdGenerator.getInstance().nextId()).build()).build())
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
