package com.miotech.kun.dataquality.mock;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.Dataset;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.core.JDBCExpectationAssertion;
import com.miotech.kun.dataquality.core.JDBCExpectationMethod;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

public class MockExpectationSpecFactory {

    private MockExpectationSpecFactory() {
    }

    public static ExpectationSpec create(Long datasetGid) {
        return ExpectationSpec.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName("Expectation Name")
                .withDescription("Expectation Desc")
                .withMethod(new JDBCExpectationMethod("select count(1) c from demo",
                        ImmutableList.of(new JDBCExpectationAssertion("c", JDBCExpectationAssertion.ComparisonOperator.EQUALS, "=", "NUMBER", "0"))))
                .withTrigger(ExpectationSpec.ExpectationTrigger.SCHEDULED)
                .withDataset(Dataset.builder().gid(datasetGid).dataSource(DataSource.newBuilder().withId(IdGenerator.getInstance().nextId()).build()).build())
                .withTaskId(IdGenerator.getInstance().nextId())
                .withIsBlocking(true)
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withCreateUser("admin")
                .withUpdateUser("admin")
                .build();
    }

    public static ExpectationSpec create() {
        return create(IdGenerator.getInstance().nextId());
    }

}
