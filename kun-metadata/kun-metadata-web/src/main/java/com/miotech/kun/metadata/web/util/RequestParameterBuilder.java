package com.miotech.kun.metadata.web.util;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.constant.WorkflowApiParam;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public class RequestParameterBuilder {

    private RequestParameterBuilder() {
    }

    public static Operator buildOperatorForCreate(String operatorName) {
        return Operator.newBuilder()
                .withName(operatorName)
                .withDescription(StringUtils.EMPTY)
                .withClassName(WorkflowApiParam.CLASS_NAME).build();
    }

    public static Task buildTaskForCreate(String taskName, Long operatorId, Properties properties) {
        Task.Builder taskBuilder = Task.newBuilder()
                .withName(taskName)
                .withDescription(StringUtils.EMPTY)
                .withOperatorId(operatorId)
                .withDependencies(Lists.newArrayList())
                .withTags(Lists.newArrayList());
        fillConfig(taskBuilder, taskName, properties);
        return taskBuilder.build();
    }

    private static void fillConfig(Task.Builder taskBuilder, String taskName, Properties properties) {
        Config config = buildConfigForCreate(taskName, properties);
        TaskParam taskParam = TaskParam.get(taskName);
        switch (taskParam) {
            case REFRESH:
                taskBuilder.withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.NONE).build());
                taskBuilder.withConfig(config);
                break;
            case BUILD_ALL:
                taskBuilder.withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.SCHEDULED)
                        .withCronExpr(properties.getProperty(PropKey.CRON_EXPR)).build());
                taskBuilder.withConfig(config);
                break;
            default:
                throw new IllegalArgumentException("Invalid taskName:" + taskName);
        }
    }

    private static Config buildConfigForCreate(String taskName, Properties properties) {
        TaskParam taskParam = TaskParam.get(taskName);
        switch (taskParam) {
            case REFRESH:
                return Config.EMPTY;
            case BUILD_ALL:
                Config.Builder confBuilder = Config.newBuilder();
                confBuilder.addConfig(PropKey.JDBC_URL, properties.getProperty(PropKey.JDBC_URL));
                confBuilder.addConfig(PropKey.USERNAME, properties.getProperty(PropKey.USERNAME));
                confBuilder.addConfig(PropKey.PASSWORD, properties.getProperty(PropKey.PASSWORD));
                confBuilder.addConfig(PropKey.DRIVER_CLASS_NAME, properties.getProperty(PropKey.DRIVER_CLASS_NAME));
                confBuilder.addConfig(PropKey.DEPLOY_MODE, DataBuilderDeployMode.ALL.name());
                return confBuilder.build();
            default:
                throw new IllegalArgumentException("Invalid taskName:" + taskName);
        }
    }

}
