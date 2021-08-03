package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SparkSubmitTaskTemplateRender extends SparkSubmitBasedTaskTemplateRender {

    @Override
    public String buildSparkSubmitCmd(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        List<String> params = buildBasicSparkCmd(taskConfig, taskDefinition);
        params.add(0, "spark-submit");

        //parse application main and args
        String appMain = (String) taskConfig.get("application");
        String appArgs = (String) taskConfig.get("args");
        params.add(appMain);
        if(appArgs != null){
            params.add(appArgs);
        }
        return String.join(" ", params);
    }

}
