package com.miotech.kun.metadata.web.util;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.Props;
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

public class RequestParameterBuilder {

    private RequestParameterBuilder() {
    }

    public static Operator buildOperatorForCreate(String operatorName) {
        return Operator.newBuilder()
                .withName(operatorName)
                .withDescription(StringUtils.EMPTY)
                .withClassName(WorkflowApiParam.CLASS_NAME).build();
    }

    public static Task buildTaskForCreate(String taskName, Long operatorId, Props props) {
        Task.Builder taskBuilder = Task.newBuilder()
                .withName(taskName)
                .withDescription(StringUtils.EMPTY)
                .withOperatorId(operatorId)
                .withDependencies(Lists.newArrayList())
                .withTags(Lists.newArrayList());
        fillConfig(taskBuilder, taskName, props);
        return taskBuilder.build();
    }

    private static void fillConfig(Task.Builder taskBuilder, String taskName, Props props) {
        Config config = buildConfigForCreate(taskName, props);
        TaskParam taskParam = TaskParam.get(taskName);
        switch (taskParam) {
            case MANUAL:
                taskBuilder.withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.NONE).build());
                taskBuilder.withConfig(config);
                break;
            case AUTO:
                taskBuilder.withScheduleConf(ScheduleConf.ScheduleConfBuilder.aScheduleConf().withType(ScheduleType.SCHEDULED)
                        .withCronExpr(props.get(PropKey.CRON_EXPR)).build());
                taskBuilder.withConfig(config);
                break;
            default:
                throw new IllegalArgumentException("Invalid taskName:" + taskName);
        }
    }

    private static Config buildConfigForCreate(String taskName, Props props) {
        TaskParam taskParam = TaskParam.get(taskName);
        Config.Builder confBuilder = Config.newBuilder();
        switch (taskParam) {
            case MANUAL:
                confBuilder.addConfig(PropKey.EXTRACT_STATS, true);
                return confBuilder.build();
            case AUTO:
                confBuilder.addConfig(PropKey.JDBC_URL, props.getString(PropKey.JDBC_URL));
                confBuilder.addConfig(PropKey.USERNAME, props.getString(PropKey.USERNAME));
                confBuilder.addConfig(PropKey.PASSWORD, props.getString(PropKey.PASSWORD));
                confBuilder.addConfig(PropKey.DRIVER_CLASS_NAME, props.getString(PropKey.DRIVER_CLASS_NAME));
                confBuilder.addConfig(PropKey.DEPLOY_MODE, DataBuilderDeployMode.ALL.name());
                confBuilder.addConfig(PropKey.EXTRACT_STATS, false);
                return confBuilder.build();
            default:
                throw new IllegalArgumentException("Invalid taskName:" + taskName);
        }
    }

}
