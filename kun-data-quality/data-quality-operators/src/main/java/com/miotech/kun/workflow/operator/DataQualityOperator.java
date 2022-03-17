package com.miotech.kun.workflow.operator;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.query.service.ConfigService;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.assertion.AssertionSample;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.*;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class DataQualityOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityOperator.class);

    private Long caseId;
    private Long caseRunId;
    private DataQualityClient dataQualityClient;

    @Override
    public void init() {
        OperatorContext context = getContext();
        ConfigService configService = ConfigService.getInstance();
        Config config = context.getConfig();
        configService.setMetadataDataSourceUrl(config.getString(METADATA_DATASOURCE_URL));
        configService.setMetadataDataSourceUsername(config.getString(DataQualityConfiguration.METADATA_DATASOURCE_USERNAME));
        configService.setMetadataDataSourcePassword(config.getString(DataQualityConfiguration.METADATA_DATASOURCE_PASSWORD));
        configService.setMetadataDataSourceDriverClass(config.getString(DataQualityConfiguration.METADATA_DATASOURCE_DIRVER_CLASS));
        configService.setInfraBaseUrl(config.getString(INFRA_BASE_URL));

        dataQualityClient = DataQualityClient.getInstance();

        String caseIdStr = context.getConfig().getString("caseId");
        if (StringUtils.isEmpty(caseIdStr)) {
            logger.error("Data quality case id is empty.");
            throw new IllegalArgumentException("Data quality case id is empty.");
        }
        caseId = Long.valueOf(caseIdStr);
        caseRunId = context.getTaskRunId();
    }

    @Override
    public boolean run() {
        ValidationResult.Builder vrb = ValidationResult.newBuilder()
                .withExpectationId(caseId)
                .withUpdateTime(DateTimeUtils.now());
        try {
            logger.info("prepare to execute the test case: {}", caseId);
            Expectation expectation = dataQualityClient.getExpectation(caseId);
            Metrics metrics = expectation.getMetrics();
            Assertion assertion = expectation.getAssertion();
            logger.info("assertion: {}", JSONUtils.toJsonString(assertion));

            MetricsCollectedResult<String> currentMetricsCollectedResult = metrics.collect();
            dataQualityClient.recordMetricsCollectedResult(expectation.getExpectationId(), currentMetricsCollectedResult);
            logger.info("metrics: {} collection completed, result: {}", JSONUtils.toJsonString(metrics),
                    JSONUtils.toJsonString(currentMetricsCollectedResult));

            MetricsCollectedResult<String> theResultCollectedNDaysAgo = dataQualityClient.getTheResultCollectedNDaysAgo(expectation.getExpectationId(),
                    assertion.getComparisonPeriod().getDaysAgo());
            logger.info("benchmark metrics: {}", JSONUtils.toJsonString(theResultCollectedNDaysAgo));
            boolean isPassed = assertion.doAssert(AssertionSample.of(currentMetricsCollectedResult, theResultCollectedNDaysAgo));
            logger.info("assertion result: {}", isPassed);
            vrb.withPassed(isPassed);
            vrb.withAssertionResults(ImmutableList.of(AssertionResult.from(metrics, assertion, currentMetricsCollectedResult, theResultCollectedNDaysAgo)));
            dataQualityClient.record(vrb.build() , caseRunId);
            return true;
        } catch (Exception e) {
            logger.error(String.format("caseId=%d %s", caseId, "Failed to run test case."), e);
            vrb.withPassed(false);
            vrb.withExecutionResult(ExceptionUtils.getStackTrace(e));
            dataQualityClient.record(vrb.build(), caseRunId);
            return false;
        }
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(METADATA_DATASOURCE_URL, ConfigDef.Type.STRING, true, "datasource connection url, like jdbc://host:port/dbname", METADATA_DATASOURCE_URL)
                .define(METADATA_DATASOURCE_USERNAME, ConfigDef.Type.STRING, true, "datasource connection username", METADATA_DATASOURCE_USERNAME)
                .define(METADATA_DATASOURCE_PASSWORD, ConfigDef.Type.STRING, true, "datasource connection password", METADATA_DATASOURCE_PASSWORD)
                .define(METADATA_DATASOURCE_DIRVER_CLASS, ConfigDef.Type.STRING, true, "datasource driver class", METADATA_DATASOURCE_DIRVER_CLASS)
                .define(DATAQUALITY_CASE_ID, ConfigDef.Type.STRING, true, "data quality case id", DATAQUALITY_CASE_ID)
                .define(INFRA_BASE_URL, ConfigDef.Type.STRING, true, "infra base url", INFRA_BASE_URL)
                ;
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

}
