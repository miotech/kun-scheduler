package com.miotech.kun.metadata.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.OperatorParam;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
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

    public void createOperator(String operatorName, String packagePath) {
        String result = httpClientUtil.doPost(workflowUrlGenerator.generateCreateOperatorUrl(),
                buildCreateOperatorParams(operatorName, packagePath));
        logger.debug("Call Create Operator result: {}", result);
        if (!WorkflowApiResponseParseUtil.isSuccess(result)) {
            logger.warn("Create Operator result: {}", result);
            throw new IllegalStateException("Create Operator Fail");
        }

        properties.setProperty(OperatorParam.get(operatorName).getOperatorKey(), WorkflowApiResponseParseUtil.parseIdAfterCreate(result).toString());
    }

    public void createTask(String taskName, Long operatorId) {
        String result = httpClientUtil.doPost(workflowUrlGenerator.generateCreateTaskUrl(),
                buildCreateTaskParams(taskName, operatorId));
        logger.debug("Call Create Task result: {}", result);
        if (!WorkflowApiResponseParseUtil.isSuccess(result)) {
            logger.warn("Create Task result: {}", result);
            throw new IllegalStateException("Create Task Fail");
        }

        properties.setProperty(TaskParam.get(taskName).getTaskKey(), WorkflowApiResponseParseUtil.parseIdAfterCreate(result).toString());
    }

    private String buildCreateOperatorParams(String operatorName, String packagePath) {
        OperatorPropsVO operatorPropsVO = OperatorPropsVO.newBuilder()
                .withName(operatorName)
                .withDescription(StringUtils.EMPTY)
                .withParams(Lists.newArrayList())
                .withPackagePath(packagePath)
                .withClassName(WorkflowApiParam.CLASS_NAME)
                .build();

        return JSONUtils.toJsonString(operatorPropsVO);
    }

    private String buildCreateTaskParams(String taskName, Long operatorId) {
        TaskPropsVO.TaskPropsVOBuilder propsVOBuilder = TaskPropsVO.newBuilder()
                .withName(taskName)
                .withDescription(StringUtils.EMPTY)
                .withOperatorId(operatorId)
                .withArguments(Lists.newArrayList())
                .withDependencies(Lists.newArrayList())
                .withTags(Lists.newArrayList());
        fillInfo(propsVOBuilder, taskName);
        return JSONUtils.toJsonString(propsVOBuilder.build());
    }

    private void fillInfo(TaskPropsVO.TaskPropsVOBuilder builder, String taskName) {
        List<Variable> variables = buildVariablesForCreate(taskName);
        TaskParam taskParam = TaskParam.get(taskName);
        switch (taskParam) {
            case REFRESH:
                builder.withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.NONE).build());
                builder.withVariableDefs(variables);
                break;
            case BUILD_ALL:
                builder.withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.SCHEDULED)
                        .withCronExpr(properties.getProperty(PropKey.CRON_EXPR)).build());
                builder.withVariableDefs(variables);
                break;
            default:
                throw new IllegalArgumentException("Invalid taskName:" + taskName);
        }
    }

    private String buildTaskRunParams(Long id, DataBuilderDeployMode deployMode) {
        List<RunTaskVO> runTaskVOs = Lists.newArrayList();

        RunTaskVO datasourceIdVO = new RunTaskVO();
        datasourceIdVO.setTaskId(Long.parseLong(properties.getProperty(TaskParam.REFRESH.getTaskKey())));
        datasourceIdVO.setVariables(buildVariablesForTaskRun(deployMode, id.toString()));

        runTaskVOs.add(datasourceIdVO);
        return JSONUtils.toJsonString(runTaskVOs);
    }

    private List<Variable> buildVariablesForCreate(String taskName) {
        TaskParam taskParam = TaskParam.get(taskName);
        switch (taskParam) {
            case REFRESH:
                return Arrays.asList(Variable.newBuilder().withKey(PropKey.JDBC_URL).build(),
                        Variable.newBuilder().withKey(PropKey.USERNAME).build(),
                        Variable.newBuilder().withKey(PropKey.PASSWORD).build(),
                        Variable.newBuilder().withKey(PropKey.DRIVER_CLASS_NAME).build(),
                        Variable.newBuilder().withKey(PropKey.DEPLOY_MODE).build(),
                        Variable.newBuilder().withKey(PropKey.DATASOURCE_ID).build(),
                        Variable.newBuilder().withKey(PropKey.GID).build());
            case BUILD_ALL:
                return Arrays.asList(Variable.newBuilder().withKey(PropKey.JDBC_URL).withValue(properties.getProperty(PropKey.JDBC_URL)).build(),
                        Variable.newBuilder().withKey(PropKey.USERNAME).withValue(properties.getProperty(PropKey.USERNAME)).build(),
                        Variable.newBuilder().withKey(PropKey.PASSWORD).withValue(properties.getProperty(PropKey.PASSWORD)).build(),
                        Variable.newBuilder().withKey(PropKey.DRIVER_CLASS_NAME).withValue(properties.getProperty(PropKey.DRIVER_CLASS_NAME)).build(),
                        Variable.newBuilder().withKey(PropKey.DEPLOY_MODE).withValue(DataBuilderDeployMode.ALL.name()).build());
            default:
                throw new IllegalArgumentException("Invalid taskName:" + taskName);
        }
    }

    private Map<String, String> buildVariablesForTaskRun(DataBuilderDeployMode deployMode, String id) {
        Map<String, String> variables = Maps.newHashMap();
        variables.put(PropKey.JDBC_URL, properties.getProperty(PropKey.JDBC_URL));
        variables.put(PropKey.USERNAME, properties.getProperty(PropKey.USERNAME));
        variables.put(PropKey.PASSWORD, properties.getProperty(PropKey.PASSWORD));
        variables.put(PropKey.DRIVER_CLASS_NAME, properties.getProperty(PropKey.DRIVER_CLASS_NAME));
        variables.put(PropKey.DEPLOY_MODE, deployMode.name());

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
