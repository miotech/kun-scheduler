package com.miotech.kun.metadata.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.WorkflowApiParam;
import com.miotech.kun.metadata.web.util.WorkflowApiResponseParseUtil;
import com.miotech.kun.metadata.web.util.WorkflowUrlGenerator;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Singleton
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    @Inject
    private HttpClientUtil httpClientUtil;

    @Inject
    private Properties properties;

    @Inject
    private WorkflowUrlGenerator workflowUrlGenerator;

    public String submit(Long id, DataBuilderDeployMode deployMode) {
        String params = buildTaskRunParams(id, deployMode);
        String result = httpClientUtil.doPost(workflowUrlGenerator.generateRunTaskUrl(), params);
        logger.debug("Call Create TasksRun result: {}", result);
        return WorkflowApiResponseParseUtil.parseProcessId(result);
    }

    public String fetchStatus(String id) {
        Preconditions.checkNotNull(id, "Invalid id: null");
        String result = httpClientUtil.doGet(workflowUrlGenerator.buildFetchStatusUrl(id));
        logger.debug("Call Fetch TaskRun Status result: {}", result);
        return result;
    }

    public void createOperator() {
        String result = httpClientUtil.doPost(workflowUrlGenerator.generateCreateOperatorUrl(), buildCreateOperatorParams());
        logger.debug("Call Create Operator result: {}", result);
        if (!WorkflowApiResponseParseUtil.isSuccess(result)) {
            logger.warn("Create Operator result: {}", result);
            throw new IllegalStateException("Create Operator Fail");
        }

        properties.setProperty(PropKey.OPERATOR_ID, WorkflowApiResponseParseUtil.parseIdAfterCreate(result).toString());
    }

    public void createTask() {
        String result = httpClientUtil.doPost(workflowUrlGenerator.generateCreateTaskUrl(), buildCreateTaskParams());
        logger.debug("Call Create Task result: {}", result);
        if (!WorkflowApiResponseParseUtil.isSuccess(result)) {
            logger.warn("Create Task result: {}", result);
            throw new IllegalStateException("Create Task Fail");
        }

        properties.setProperty(PropKey.TASK_ID, WorkflowApiResponseParseUtil.parseIdAfterCreate(result).toString());
    }

    private String buildCreateOperatorParams() {
        OperatorPropsVO operatorPropsVO = OperatorPropsVO.newBuilder()
                .withName(WorkflowApiParam.OPERATOR_NAME)
                .withDescription(StringUtils.EMPTY)
                .withParams(Lists.newArrayList())
                .withPackagePath(WorkflowApiParam.PACKAGE_PATH)
                .withClassName(WorkflowApiParam.CLASS_NAME)
                .build();

        return JSONUtils.toJsonString(operatorPropsVO);
    }

    private String buildCreateTaskParams() {
        List<Variable> variables = Arrays.asList(Variable.newBuilder().withKey(PropKey.JDBC_URL).build(),
                Variable.newBuilder().withKey(PropKey.USERNAME).build(),
                Variable.newBuilder().withKey(PropKey.PASSWORD).build(),
                Variable.newBuilder().withKey(PropKey.DRIVER_CLASS_NAME).build(),
                Variable.newBuilder().withKey(PropKey.DEPLOY_MODE).build(),
                Variable.newBuilder().withKey(PropKey.DATASOURCE_ID).build(),
                Variable.newBuilder().withKey(PropKey.GID).build());


        TaskPropsVO taskPropsVO = TaskPropsVO.newBuilder()
                .withName(WorkflowApiParam.TASK_NAME)
                .withDescription(StringUtils.EMPTY)
                .withOperatorId(Long.parseLong(properties.getProperty(PropKey.OPERATOR_ID)))
                .withArguments(Lists.newArrayList())
                .withVariableDefs(variables)
                .withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.NONE).build())
                .withDependencies(Lists.newArrayList())
                .build();

        return JSONUtils.toJsonString(taskPropsVO);
    }

    private String buildTaskRunParams(Long id, DataBuilderDeployMode deployMode) {
        List<RunTaskVO> runTaskVOs = Lists.newArrayList();

        RunTaskVO datasourceIdVO = new RunTaskVO();
        datasourceIdVO.setTaskId(Long.parseLong(properties.getProperty(PropKey.TASK_ID)));
        datasourceIdVO.setVariables(buildVariables(deployMode, id.toString()));

        runTaskVOs.add(datasourceIdVO);
        return JSONUtils.toJsonString(runTaskVOs);
    }

    private Map<String, String> buildVariables(DataBuilderDeployMode deployMode, String id) {
        Map<String, String> variables = Maps.newHashMap();
        variables.put(PropKey.DEPLOY_MODE, deployMode.name());
        variables.put(PropKey.JDBC_URL, properties.getProperty(PropKey.JDBC_URL));
        variables.put(PropKey.USERNAME, properties.getProperty(PropKey.USERNAME));
        variables.put(PropKey.PASSWORD, properties.getProperty(PropKey.PASSWORD));
        variables.put(PropKey.DRIVER_CLASS_NAME, properties.getProperty(PropKey.DRIVER_CLASS_NAME));

        switch (deployMode) {
            case DATASOURCE:
                variables.put(PropKey.DATASOURCE_ID, id);
                break;
            case DATASET:
                variables.put(PropKey.GID, id);
                break;
            default:
                throw new UnsupportedOperationException("Invalid deployMode:" + deployMode);
        }

        return variables;
    }
}
