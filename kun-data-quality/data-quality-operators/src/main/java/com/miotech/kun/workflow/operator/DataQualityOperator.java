package com.miotech.kun.workflow.operator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.MapProps;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsProvider;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.assertion.AssertionSample;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.hooks.DataQualityCheckOperationHook;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.dataquality.core.model.DataQualityOperatorContext;
import com.miotech.kun.dataquality.core.model.OperatorHookParams;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
    private DataQualityOperatorContext dataQualityContext;
    private DataQualityCheckOperationHook operationHook;

    private Props props;
    private Injector injector;

    @Override
    public void init() {
        OperatorContext context = getContext();
        Config config = context.getConfig();

        props = buildPropsFromVariable(config);
        injector = Guice.createInjector(new DataQualityModule(props));

        dataQualityClient = injector.getInstance(DataQualityClient.class);
        String caseIdStr = props.getString(DATAQUALITY_CASE_ID);
        if (StringUtils.isEmpty(caseIdStr)) {
            logger.error("Data quality case id is empty.");
            throw new IllegalArgumentException("Data quality case id is empty.");
        }
        caseId = Long.valueOf(caseIdStr);
        caseRunId = context.getTaskRunId();

        String datasetJson = props.getString(VALIDATE_DATASET);
        logger.debug("trigger dataset is " + datasetJson);
        Dataset triggerDataset = JSONUtils.jsonToObject(datasetJson, Dataset.class);
        logger.debug("building data quality operator context with dataset {}", triggerDataset.getGid());
        dataQualityContext = DataQualityOperatorContext.newBuilder()
                .withDataset(triggerDataset)
                .build();
        String className = props.getString(OPERATOR_HOOK_CLASS);
        OperatorHookParams hookParams = new OperatorHookParams();
        hookParams.setParams(JSONUtils.jsonStringToStringMap(props.getString(OPERATOR_HOOK_PARAMS)));
        operationHook = initHook(className, hookParams);
    }



    @Override
    public boolean run() {
        ValidationResult.Builder vrb = ValidationResult.newBuilder()
                .withExpectationId(caseId)
                .withUpdateTime(DateTimeUtils.now());
        try {
            logger.info("prepare to execute the test case: {}", caseId);
            Expectation expectation = dataQualityClient.findById(caseId);
            Metrics metrics = expectation.getMetrics();
            Assertion assertion = expectation.getAssertion();
            logger.info("assertion: {}", JSONUtils.toJsonString(assertion));

            beforeExecute();
            MetricsCollectedResult<String> currentMetricsCollectedResult = metrics.collect();
            afterExecute();

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
                .define(VALIDATE_DATASET, ConfigDef.Type.STRING, true, "validate dataset", VALIDATE_DATASET)
                .define(OPERATOR_HOOK_CLASS, ConfigDef.Type.STRING, true, "operator hook class", OPERATOR_HOOK_CLASS)
                .define(OPERATOR_HOOK_PARAMS, ConfigDef.Type.STRING, true, "operator hook params", OPERATOR_HOOK_PARAMS);
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    private Props buildPropsFromVariable(Config config) {
        Props props = new Props();
        Map<String,Object> map = new HashMap<>();

        map.put(METADATA_DATASOURCE_URL, config.getString(METADATA_DATASOURCE_URL));
        map.put(METADATA_DATASOURCE_USERNAME, config.getString(METADATA_DATASOURCE_USERNAME));
        map.put(METADATA_DATASOURCE_PASSWORD, config.getString(METADATA_DATASOURCE_PASSWORD));
        map.put(METADATA_DATASOURCE_DIRVER_CLASS, config.getString(METADATA_DATASOURCE_DIRVER_CLASS));
        map.put(DATAQUALITY_CASE_ID, config.getString(DATAQUALITY_CASE_ID));
        map.put(INFRA_BASE_URL, config.getString(INFRA_BASE_URL));
        map.put(VALIDATE_DATASET, config.getString(VALIDATE_DATASET));
        map.put(OPERATOR_HOOK_CLASS, config.getString(OPERATOR_HOOK_CLASS));
        map.put(OPERATOR_HOOK_PARAMS, config.getString(OPERATOR_HOOK_PARAMS));

        PropsProvider runTimeProvider = new MapProps(map);
        props.addPropsProvider(runTimeProvider);
        return props;
    }

    private DataQualityCheckOperationHook initHook(String className, OperatorHookParams hookParams) {
        if (className == null) {
            return null;
        }
        try {
            logger.debug("using {} for operator hook", className);
            Class clazz = Class.forName(className);
            DataQualityCheckOperationHook hook = (DataQualityCheckOperationHook) clazz.
                    getDeclaredConstructor().newInstance();
            logger.debug("init hook with params:{}", hookParams);
            hook.initialize(hookParams);
            return hook;
        } catch (Exception e) {
            logger.error("could not create data quality operator check hook", e);
            return null;
        }
    }

    private void beforeExecute() {
        if (operationHook != null) {
            try {
                operationHook.before(dataQualityContext);
            } catch (Exception e) {
                logger.error("hook before execute failed", e);
            }
        }
    }

    private void afterExecute() {
        if (operationHook != null) {
            try {
                operationHook.after(dataQualityContext);
            } catch (Exception e) {
                logger.error("hook after execute failed", e);
            }
        }
    }

}
