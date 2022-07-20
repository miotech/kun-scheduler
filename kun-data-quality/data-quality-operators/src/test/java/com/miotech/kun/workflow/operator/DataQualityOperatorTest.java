package com.miotech.kun.workflow.operator;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
public class DataQualityOperatorTest extends DatabaseTestBase {

    private KunOperator operator;
    private OperatorRunner operatorRunner;
    private Long expectationId;

    @Inject
    private DataQualityClient dataQualityClient;

    @Override
    protected void configuration() {
        super.configuration();
        Props props = new Props();
        props.put(INFRA_BASE_URL, "http://kun-infra");
        bind(Props.class, props);
        setFlywayLocation("webapp_sql");
    }

    @BeforeEach
    public void setUp() {
        expectationId = IdGenerator.getInstance().nextId();
        operator = new DataQualityOperator();
        operatorRunner = new OperatorRunner(operator);

        Map<String, Object> params = new HashMap<>();
        params.put(METADATA_DATASOURCE_URL, postgres.getJdbcUrl());
        params.put(METADATA_DATASOURCE_USERNAME, postgres.getUsername());
        params.put(METADATA_DATASOURCE_PASSWORD, postgres.getPassword());
        params.put(METADATA_DATASOURCE_DIRVER_CLASS, "org.postgresql.Driver");
        params.put(DATAQUALITY_CASE_ID, String.valueOf(expectationId));
        params.put(VALIDATE_DATASET, JSONUtils.toJsonString(Dataset.builder().gid(IdGenerator.getInstance().nextId()).build()));
        params.put(OPERATOR_HOOK_PARAMS, JSONUtils.toJsonString(Maps.newHashMap()));
        operatorRunner.setConfig(params);
    }

    @Test
    public void run() {
        operatorRunner.run();

        List<ValidationResult> validationResults = dataQualityClient.fetchByExpectationId(expectationId);
        assertThat(validationResults.size(), is(1));
    }

}
