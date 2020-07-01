package com.miotech.kun.metadata.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.List;
import java.util.Properties;

@Singleton
public class ProcessService {

    @Inject
    private HttpClientUtil httpClientUtil;

    @Inject
    private Properties properties;

    public String submit(Long id, DataBuilderDeployMode deployMode) {
        // 读取Operator id
        Long operatorId = Long.parseLong(properties.getProperty("workflow.operatorId"));

        // 创建Task
        TaskPropsVO taskBody = buildTaskPropsVO(operatorId, id, deployMode);
        String createTaskUrl = buildCreateTaskUrl();

        return httpClientUtil.doPost(createTaskUrl, JSONUtils.toJsonString(taskBody));
    }

    private TaskPropsVO buildTaskPropsVO(Long operatorId, Long id, DataBuilderDeployMode deployMode) {
        TaskPropsVO.TaskPropsVOBuilder propsVOBuilder = TaskPropsVO.newBuilder();
        propsVOBuilder.withOperatorId(operatorId);

        List<Variable> variables = initVariables(deployMode);
        switch (deployMode) {
            case DATASOURCE:
                Variable datasourceIdVariable = Variable.newBuilder().withKey("datasourceId").withValue(id.toString()).build();
                variables.add(datasourceIdVariable);

                propsVOBuilder.withName("DataBuilder Daily Task")
                        .withDescription("Daily full pull task")
                        .withVariableDefs(variables)
                        .withScheduleConf(new ScheduleConf(ScheduleType.ONESHOT, null));
                break;
            case DATASET:
                Variable gidVariable = Variable.newBuilder().withKey("gid").withValue(id.toString()).build();
                variables.add(gidVariable);

                propsVOBuilder.withName("DataBuilder Daily Task")
                        .withDescription("Daily full pull task")
                        .withVariableDefs(variables)
                        .withScheduleConf(new ScheduleConf(ScheduleType.ONESHOT, null));
                break;
            default:
                throw new UnsupportedOperationException("Invalid deployMode:" + deployMode);
        }

        return propsVOBuilder.build();
    }

    private List<Variable> initVariables(DataBuilderDeployMode deployMode) {
        List<Variable> variables = Lists.newArrayList();

        Variable env = Variable.newBuilder().withKey("env").withValue(properties.getProperty("env")).build();
        variables.add(env);
        Variable deployModeVariable = Variable.newBuilder().withKey("deploy-mode").withValue(deployMode.name()).build();
        variables.add(deployModeVariable);

        return variables;
    }

    private String buildCreateTaskUrl() {
        String baseUrl = "http://%s/tasks";
        String workflowUrl = properties.getProperty("workflow.url");
        return String.format(baseUrl, workflowUrl);
    }

    private String buildFetchStatusUrl(String id) {
        String baseUrl = "http://%s/taskruns/%s/status";
        String workflowUrl = properties.getProperty("workflow.url");
        return String.format(baseUrl, workflowUrl, id);
    }

    public String fetchStatus(String id) {
        Preconditions.checkNotNull(id, "Invalid id: null");
        return httpClientUtil.doGet(buildFetchStatusUrl(id));
    }

}
