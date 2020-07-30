package com.miotech.kun.dataplatform.common.tasktemplate.service;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.common.tasktemplate.dao.TaskTemplateDao;
import com.miotech.kun.dataplatform.common.tasktemplate.renderer.DefaultTaskTemplateRenderer;
import com.miotech.kun.dataplatform.common.tasktemplate.renderer.TaskTemplateRenderer;
import com.miotech.kun.dataplatform.common.tasktemplate.vo.TaskTemplateVO;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TaskTemplateService {

    @Autowired
    private TaskTemplateDao taskTemplateDao;

    @Autowired
    private ApplicationContext applicationContext;

    public List<TaskTemplate> getAllTaskTemplates() {
        return taskTemplateDao.getTaskTemplates();
    }

    public TaskTemplate find(String taskTemplateName) {
        return taskTemplateDao.fetchByName(taskTemplateName)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException(String.format("Task Template not found: \"%s\"", taskTemplateName));
                });
    }

    public TaskConfig getTaskConfig(Map<String, Object> taskConfig, String taskTemplateName) {
        return getTaskConfig(taskConfig, find(taskTemplateName));
    }

    public TaskConfig getTaskConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        String renderClass = taskTemplate.getRenderClassName() != null
                ? taskTemplate.getRenderClassName()
                : DefaultTaskTemplateRenderer.class.getName();

        try {
            Class<?> render = Class.forName(renderClass);
            assert renderClass != null;
            return ((TaskTemplateRenderer) applicationContext.getBean(render))
            .render(taskConfig, taskTemplate);
        } catch (Exception e) {
            log.error("", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public TaskTemplateVO convertToVO(TaskTemplate taskTemplate) {
        return new TaskTemplateVO(
                taskTemplate.getName(),
                taskTemplate.getTemplateType(),
                taskTemplate.getTemplateGroup(),
                taskTemplate.getDisplayParameters()
        );
    }
}
