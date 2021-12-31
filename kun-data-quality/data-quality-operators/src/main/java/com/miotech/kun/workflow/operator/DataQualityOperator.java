package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.query.service.ConfigService;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.Expectation;
import com.miotech.kun.dataquality.core.ExpectationFactory;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.core.ValidationResult;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
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
        try {
            ExpectationSpec expectationSpec = dataQualityClient.getExpectation(caseId);
            Expectation expectation = ExpectationFactory.get(expectationSpec);
            ValidationResult vr = expectation.validate();
            dataQualityClient.record(vr, caseRunId);
            return true;
        } catch (Exception e) {
            logger.error(String.format("caseId=%d %s", caseId, "Failed to run test case."), e);
            ValidationResult.Builder vrb = ValidationResult.newBuilder();
            vrb.withExpectationId(caseId);
            vrb.withPassed(false);
            vrb.withExecutionResult(ExceptionUtils.getStackTrace(e));
            vrb.withUpdateTime(DateTimeUtils.now());
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
