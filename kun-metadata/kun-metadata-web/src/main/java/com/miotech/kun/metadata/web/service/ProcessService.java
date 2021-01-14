package com.miotech.kun.metadata.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;

import java.util.Map;

@Singleton
public class ProcessService {

    @Inject
    private WorkflowClient workflowClient;

    @Inject
    private Props props;

    public String submit(Long id, DataBuilderDeployMode deployMode) {
        TaskRun taskRun = workflowClient.executeTask(props.getLong(TaskParam.MCE_TASK.getName()),
                buildVariablesForTaskRun(deployMode, id.toString()));
        return taskRun.getId().toString();
    }

    public TaskRun fetchStatus(String id) {
        Preconditions.checkNotNull(id, "Invalid id: null");
        return workflowClient.getTaskRun(Long.parseLong(id));
    }

    private Map<String, Object> buildVariablesForTaskRun(DataBuilderDeployMode deployMode, String id) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(PropKey.JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(PropKey.USERNAME, props.get(PropKey.USERNAME));
        conf.put(PropKey.PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(PropKey.DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(PropKey.DEPLOY_MODE, deployMode.name());
        conf.put(PropKey.BROKERS, props.get("kafka.bootstrapServers"));
        conf.put(PropKey.MSE_TOPIC, props.get("kafka.mseTopicName"));

        switch (deployMode) {
            case DATASOURCE:
                conf.put(PropKey.DATASOURCE_ID, id);
                break;
            case DATASET:
                conf.put(PropKey.GID, id);
                break;
            default:
                throw new UnsupportedOperationException("Invalid deployMode:" + deployMode);
        }

        return conf;
    }
}
