package com.miotech.kun.metadata.web.util;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.OperatorParam;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestParameterBuilder {

    private static final String TASK_NAME_PATTERN = "(mce-task-auto:)(?<dataSourceId>\\d+)";

    private RequestParameterBuilder() {
    }

    public static Operator buildOperatorForCreate(String operatorName) {
        OperatorParam operatorParam = OperatorParam.convert(operatorName);
        return Operator.newBuilder()
                .withName(operatorName)
                .withDescription(operatorParam.getDescription())
                .withClassName(operatorParam.getClassName()).build();
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
        taskBuilder.withConfig(config);
        if (isScheduleTask(taskName)) {
            taskBuilder.withScheduleConf(ScheduleConf.newBuilder().withType(ScheduleType.SCHEDULED)
                    .withCronExpr(props.get(PropKey.CRON_EXPR)).build());
        } else {
            taskBuilder.withScheduleConf(ScheduleConf.newBuilder().withType(ScheduleType.NONE).build());
        }
    }

    private static Config buildConfigForCreate(String taskName, Props props) {
        Config.Builder confBuilder = Config.newBuilder();

        if (!isScheduleTask(taskName)) {
            return confBuilder.build();
        }

        String dataSourceId = parseDataSourceId(taskName);
        confBuilder.addConfig(PropKey.JDBC_URL, props.getString(PropKey.JDBC_URL));
        confBuilder.addConfig(PropKey.USERNAME, props.getString(PropKey.USERNAME));
        confBuilder.addConfig(PropKey.PASSWORD, props.getString(PropKey.PASSWORD));
        confBuilder.addConfig(PropKey.DRIVER_CLASS_NAME, props.getString(PropKey.DRIVER_CLASS_NAME));
        confBuilder.addConfig(PropKey.DEPLOY_MODE, DataBuilderDeployMode.DATASOURCE.name());
        confBuilder.addConfig(PropKey.DATASOURCE_ID, dataSourceId);
        confBuilder.addConfig(PropKey.BROKERS, props.getString("kafka.bootstrapServers"));
        confBuilder.addConfig(PropKey.MSE_TOPIC, props.getString("kafka.mseTopicName"));

        return confBuilder.build();
    }

    private static boolean isScheduleTask(String taskName) {
        return Pattern.matches(TASK_NAME_PATTERN, taskName);
    }

    private static String parseDataSourceId(String taskName) {
        if (Pattern.matches(TASK_NAME_PATTERN, taskName)) {
            Matcher matcher = Pattern.compile(TASK_NAME_PATTERN).matcher(taskName);
            if (matcher.find()) {
                int start = matcher.start("dataSourceId");
                return taskName.substring(start);
            }
        }

        return null;
    }

}
