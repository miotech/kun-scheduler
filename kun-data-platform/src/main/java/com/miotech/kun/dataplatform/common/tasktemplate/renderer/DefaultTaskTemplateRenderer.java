package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultTaskTemplateRenderer extends TaskTemplateRenderer {

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        Map<String, Object> configMap = buildTaskConfig(taskConfig, taskTemplate);

        return TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }
}
