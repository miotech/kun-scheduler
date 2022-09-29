package com.miotech.kun.openapi.model.response;

import com.miotech.kun.dataplatform.web.model.tasktemplate.TaskTemplate;
import lombok.Data;

@Data
public class TaskTemplateVO {
    private final String taskTemplateName;

    public static TaskTemplateVO from(TaskTemplate taskTemplate) {
        return new TaskTemplateVO(taskTemplate.getName());
    }
}
