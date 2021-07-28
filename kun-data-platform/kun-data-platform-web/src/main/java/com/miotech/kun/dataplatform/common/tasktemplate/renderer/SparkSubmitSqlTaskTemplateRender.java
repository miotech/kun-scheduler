package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SparkSubmitSqlTaskTemplateRender extends SparkSubmitBasedTaskTemplateRender {

    @Override
    public String buildSparkSubmitCmd(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        List<String> params = buildBasicSparkCmd(taskConfig);
        params.add(0, "spark-sql");

        //parse sql
        String sql = (String) taskConfig.get("sparkSQL");
        params.add("-e " + "'" + sql + "'");
        return String.join(" ", params);
    }

}
