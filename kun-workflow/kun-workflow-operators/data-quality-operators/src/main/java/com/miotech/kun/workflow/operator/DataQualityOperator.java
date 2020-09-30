package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.query.JDBCQuery;
import com.miotech.kun.commons.query.JDBCQueryExecutor;
import com.miotech.kun.commons.query.datasource.DataSourceContainer;
import com.miotech.kun.commons.query.datasource.MetadataDataSource;
import com.miotech.kun.commons.query.model.QueryResultSet;
import com.miotech.kun.commons.query.service.ConfigService;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
import com.miotech.kun.workflow.operator.client.MetadataClient;
import com.miotech.kun.workflow.operator.model.*;
import com.miotech.kun.workflow.operator.utils.AssertUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.*;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class DataQualityOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityOperator.class);

    private Long caseId;

    private DataQualityRecord dataQualityRecord;

    private DataQualityClient dataQualityClient;

    private MetadataClient metadataClient;

    private static final String CASE_FAIL_MSG_PREFIX = "CASE FAIL - ";

    private static final String CASE_SUCCESS_MSG_PREFIX = "CASE SUCCESS - ";

    @Override
    public void init() {
        OperatorContext context = getContext();
        ConfigService configService = ConfigService.getInstance();
        Config config = context.getConfig();
        configService.setMetadataDataSourceUrl(config.getString(METADATA_DATASOURCE_URL));
        configService.setMetadataDataSourceUsername(config.getString(DataQualityConfiguration.METADATA_DATASOURCE_USERNAME));
        configService.setMetadataDataSourcePassword(config.getString(DataQualityConfiguration.METADATA_DATASOURCE_PASSWORD));
        configService.setMetadataDataSourceDriverClass(config.getString(DataQualityConfiguration.METADATA_DATASOURCE_DIRVER_CLASS));

        dataQualityClient = DataQualityClient.getInstance();
        metadataClient = MetadataClient.getInstance();

        String caseIdStr = context.getConfig().getString("caseId");
        if (StringUtils.isEmpty(caseIdStr)) {
            logError("Data quality case id is empty.");
            throw new RuntimeException("Data quality case id is empty.");
        }
        caseId = Long.valueOf(caseIdStr);

        dataQualityRecord = new DataQualityRecord();
        dataQualityRecord.setCaseId(caseId);
        dataQualityRecord.setStartTime(System.currentTimeMillis());
        dataQualityRecord.setCaseStatus(CaseStatus.SUCCESS.name());
    }

    @Override
    public boolean run() {
        try {
            return doRun();
        } catch (Exception e) {
            logError("Failed to run test case.", e);
            dataQualityRecord.setCaseId(caseId);
            dataQualityRecord.setCaseStatus(CaseStatus.FAILED.name());
            dataQualityRecord.appendErrorReason(e.getMessage());
            DataQualityCaseMetrics metrics = new DataQualityCaseMetrics();
            metrics.setCaseId(caseId);
            metrics.setCaseStatus(CaseStatus.FAILED);
            metrics.setErrorReason(e.getMessage());
            dataQualityClient.recordCaseMetrics(metrics);
            return false;
        } finally {
            logInfo(DataSourceContainer.getInstance().toString());
            MetadataDataSource.getInstance().cleanUp();
            DataSourceContainer.getInstance().cleanUp();
            dataQualityRecord.setEndTime(System.currentTimeMillis());
            logger.info("DQ_RECORD>>>" + JSONUtils.toJsonString(dataQualityRecord));
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
                ;
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    private boolean doRun() {
        DataQualityCase dataQualityCase = dataQualityClient.getCase(caseId);

        TemplateType dimension = dataQualityCase.getDimension();
        List<DataQualityRule> rules = dataQualityCase.getRules();

        List<Long> datasetIds = dataQualityCase.getDatasetIds();
        Dataset currentDataset = metadataClient.getDataset(datasetIds.get(0));

        List<String> queryStrings = new ArrayList<>();
        if (dimension == TemplateType.TABLE) {
            // Table Dimension
            String templateString = dataQualityCase.getExecutionString();
            Map<String, Object> args = new HashMap<>();
            args.put("table", currentDataset.getName());
            try {
                String queryString = parseTemplate(templateString, args);
                queryStrings.add(queryString);
            } catch (Exception e) {
                throwError("Failed to parse template string.");
            }

        } else if (dimension == TemplateType.FIELD) {
            // Field Dimension
            String templateString = dataQualityCase.getExecutionString();

            List<DatasetField> datasetFields = metadataClient.getDatasetFields(dataQualityClient.getDatasetFieldIdsByCaseId(caseId));
            Map<String, Object> args = new HashMap<>();
            args.put("table", currentDataset.getName());

            for (DatasetField datasetField : datasetFields) {
                args.put("field", datasetField.getName());
                try {
                    String queryString = parseTemplate(templateString, args);
                    queryStrings.add(queryString);
                } catch (Exception e) {
                    throwError("Failed to parse template string.");
                }
                args.remove("field");
            }

        } else if (dimension == TemplateType.CUSTOMIZE) {
            // Custom Dimension
            String queryString = dataQualityCase.getExecutionString();
            queryStrings.add(queryString);
        } else {
            throwError("Unsupported template type: " + dimension.name());
        }

        for (String queryString : queryStrings) {
            QueryResultSet queryResultSet = query(queryString, currentDataset);
            doAssert(queryResultSet, dimension, rules);
        }

        return true;
    }

    private void logError(String msg) {
        logError(msg, null);
    }

    private void logError(String msg, Throwable throwable) {
        logger.error(String.format("caseId=%d %s", caseId, msg), throwable);
    }

    private void logInfo(String msg) {
        logger.info(String.format("caseId=%d %s", caseId, msg));
    }

    private void logCaseFail(String msg) {
        logInfo(CASE_FAIL_MSG_PREFIX + msg);
    }

    private void logCaseFail(Object originalValue, DataQualityRule rule) {
        logCase(CASE_FAIL_MSG_PREFIX, originalValue, rule);
    }

    private void logCaseSuccess(Object originalValue, DataQualityRule rule) {
        logCase(CASE_SUCCESS_MSG_PREFIX, originalValue, rule);
    }

    private void logCase(String msgPrefix, Object originalValue, DataQualityRule rule) {
        logInfo(msgPrefix + getRecordErrorReason(originalValue, rule));
    }

    private String getRecordErrorReason(Object originalValue,
                                        DataQualityRule rule) {
        String originalValueStr = "$null";
        if (originalValue != null) {
            originalValueStr = originalValue.toString();
        }
        return "originalValue: " + originalValueStr + " rule: " + rule.toString();
    }

    private void throwError(String errorMsg) {
        throw ExceptionUtils.wrapIfChecked(new RuntimeException(errorMsg));
    }

    private void doRuleAssert(Object originalValue,
                              DataQualityRule rule) {
        boolean ruleCase = AssertUtils.doAssert(rule.getExpectedType(),
                rule.getOperator(),
                originalValue,
                rule.getExpectedValue());

        if (ruleCase) {
            logCaseSuccess(originalValue, rule);
            DataQualityCaseMetrics metrics = new DataQualityCaseMetrics();
            metrics.setCaseId(caseId);
            metrics.setCaseStatus(CaseStatus.SUCCESS);
            dataQualityClient.recordCaseMetrics(metrics);
        } else {
            logCaseFail(originalValue, rule);
            dataQualityRecord.setCaseStatus(CaseStatus.FAILED.name());
            dataQualityRecord.appendErrorReason(getRecordErrorReason(originalValue, rule));
            DataQualityCaseMetrics metrics = new DataQualityCaseMetrics();
            metrics.setCaseId(caseId);
            metrics.setCaseStatus(CaseStatus.FAILED);
            metrics.setErrorReason(getRecordErrorReason(originalValue, rule));
            dataQualityClient.recordCaseMetrics(metrics);
        }
    }

    private void doAssert(QueryResultSet resultSet,
                          TemplateType templateType,
                          List<DataQualityRule> rules) {
        if (CollectionUtils.isEmpty(resultSet.getResultSet())) {
            throwError("SQL query return empty result set.");
        }
        Map<String, ?> row = resultSet.getResultSet().get(0);
        if (templateType == TemplateType.CUSTOMIZE) {
            for (DataQualityRule rule : rules) {
                Object originalValue = row.get(rule.getField());
                doRuleAssert(originalValue, rule);
            }
        } else {
            Object originalValue = row.values().iterator().next();
            for (DataQualityRule rule : rules) {
                doRuleAssert(originalValue, rule);
            }
        }
    }

    private QueryResultSet query(String queryString, Dataset dataset) {

        JDBCQuery query = JDBCQuery.newBuilder()
                .datasetId(dataset.getId())
                .queryString(queryString)
                .build();

        return JDBCQueryExecutor.getInstance().execute(query);
    }

    private String parseTemplate(String sourceString, Map<String, Object> args) throws IOException, TemplateException {
        Template template = new Template("parseTemplate", sourceString, new Configuration(new Version("2.3.30")));
        StringWriter result = new StringWriter();
        template.process(args, result);
        return result.toString();
    }
}
