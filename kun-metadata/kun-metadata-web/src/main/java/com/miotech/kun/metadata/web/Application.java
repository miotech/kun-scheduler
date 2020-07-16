package com.miotech.kun.metadata.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.KunWebServer;
import com.miotech.kun.commons.web.module.CommonModule;
import com.miotech.kun.commons.web.module.KunWebServerModule;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.web.constant.OperatorParam;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.constant.WorkflowApiParam;
import com.miotech.kun.metadata.web.service.ProcessService;
import com.miotech.kun.metadata.web.util.WorkflowApiResponseParseUtil;
import com.miotech.kun.metadata.web.util.WorkflowUrlGenerator;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Singleton
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final Properties props;
    private final DataSource dataSource;
    private WorkflowUrlGenerator workflowUrlGenerator;
    private HttpClientUtil httpClientUtil;
    private ProcessService processService;

    @Inject
    public Application(Properties props, DataSource dataSource, WorkflowUrlGenerator workflowUrlGenerator,
                       HttpClientUtil httpClientUtil, ProcessService processService) {
        this.props = props;
        this.dataSource = dataSource;
        this.workflowUrlGenerator = workflowUrlGenerator;
        this.httpClientUtil = httpClientUtil;
        this.processService = processService;
    }

    public static void main(String[] args) {
        logger.info("Starting Jetty Kun Web Server...");

        /* Initialize Guice Injector */
        Properties props = PropertyUtils.loadAppProps();
        final Injector injector = Guice.createInjector(
                new KunWebServerModule(props),
                new CommonModule(),
                new PackageScanModule()
        );

        injector.getInstance(Application.class).start();
        injector.getInstance(KunWebServer.class).start();
    }

    private void start() {
        checkOperator(WorkflowApiParam.OPERATOR_NAME_REFRESH, WorkflowApiParam.OPERATOR_NAME_BUILD_ALL);
        checkTask(WorkflowApiParam.TASK_NAME_REFRESH, WorkflowApiParam.TASK_NAME_BUILD_ALL);
        configureDB();
    }

    private void checkOperator(String... operatorNames) {
        for (String operatorName : operatorNames) {
            String result = httpClientUtil.doGet(workflowUrlGenerator.generateSearchOperatorUrl(operatorName));
            logger.debug("Call Search Operator result: {}", result);
            if (!WorkflowApiResponseParseUtil.judgeOperatorExists(result, operatorName)) {
                processService.createOperator(operatorName, OperatorParam.get(operatorName).getPackagePath());
                logger.info("Create Operator Success");
            } else {
                props.setProperty(OperatorParam.get(operatorName).getOperatorKey(),
                        WorkflowApiResponseParseUtil.parseOperatorIdAfterSearch(result, operatorName).toString());
            }
        }
    }

    private void checkTask(String... taskNames) {
        for (String taskName : taskNames) {
            String result = httpClientUtil.doGet(workflowUrlGenerator.generateSearchTaskUrl(taskName));
            logger.debug("Call Search Task result: {}", result);
            if (!WorkflowApiResponseParseUtil.judgeTaskExists(result, props.getProperty(TaskParam.get(taskName).getOperatorKey()), taskName)) {
                processService.createTask(taskName, Long.parseLong(props.getProperty(TaskParam.get(taskName).getOperatorKey())));
                logger.info("Create Task Success");
            } else {
                props.setProperty(TaskParam.get(taskName).getTaskKey(), WorkflowApiResponseParseUtil.parseTaskIdAfterSearch(result,
                        Long.parseLong(props.getProperty(TaskParam.get(taskName).getOperatorKey())), taskName).toString());
            }
        }
    }

    private void configureDB() {
        String migrationDir = props.getProperty(ConfigurationKeys.PROP_FLYWAY_MIRGRATION, "sql/");
        String schemaHistory = props.getProperty(ConfigurationKeys.PROP_FLYWAY_TABLENAME, DatabaseSetup.DEFAULT_SCHEMA_HISTORY_TABLE);
        DatabaseSetup setup = new DatabaseSetup(schemaHistory, dataSource, migrationDir);
        setup.start();
    }

}
