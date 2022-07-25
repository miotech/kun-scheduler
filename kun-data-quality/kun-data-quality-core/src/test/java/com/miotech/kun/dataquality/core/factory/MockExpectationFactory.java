package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.connection.PostgresConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

import java.util.Map;

public class MockExpectationFactory {

    private MockExpectationFactory() {
    }

    public static Expectation create(Long datasetGid) {
        return create(datasetGid, null);
    }

    public static Expectation create(Long datasetGid, Map<String, Object> payload) {
        Dataset dataset = Dataset.builder().gid(datasetGid).dataSource(DataSource.newBuilder().withId(IdGenerator.getInstance().nextId()).build()).build();
        ExpectationTemplate expectationTemplate = MockExpectationTemplateFactory.create();
        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName("Expectation Name")
                .withDescription("Expectation Desc")
                .withGranularity(Metrics.Granularity.CUSTOM.name())
                .withTemplate(expectationTemplate)
                .withPayload(payload)
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

    public static Expectation createWithTaskId(Long taskId, ExpectationTemplate template, Map<String, Object> payload, PostgresConnectionInfo connectionInfo) {
        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName("Expectation Name")
                .withDescription("Expectation Desc")
                .withGranularity(Metrics.Granularity.CUSTOM.name())
                .withTemplate(template)
                .withPayload(payload)
                .withTrigger(Expectation.ExpectationTrigger.SCHEDULED)
                .withDataset(Dataset.builder().gid(IdGenerator.getInstance().nextId()).dataSource(DataSource.newBuilder().withId(IdGenerator.getInstance().nextId())
                        .withConnectionConfig(ConnectionConfig.newBuilder().withDataConnection(connectionInfo).build()).build()).build())
                .withTaskId(taskId)
                .withCaseType(CaseType.BLOCK)
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withCreateUser("admin")
                .withUpdateUser("admin")
                .build();
    }

    public static Expectation createCustomSQLExpectation(ExpectationTemplate template, Map<String, Object> payload, PostgresConnectionInfo connectionInfo) {
        Long taskId = IdGenerator.getInstance().nextId();
        return createWithTaskId(taskId, template, payload, connectionInfo);
    }

    public static Expectation create() {
        return create(IdGenerator.getInstance().nextId(), null);
    }

    public static Expectation create(Map<String, Object> payload) {
        return create(IdGenerator.getInstance().nextId(), payload);
    }

}
