package com.miotech.kun.dataplatform.common.tasktemplate.dao;

import com.google.common.base.Preconditions;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TaskTemplateDaoImpl implements TaskTemplateDao {
    private final static Logger logger = LoggerFactory.getLogger(TaskTemplateDaoImpl.class);

    @Autowired
    private TaskTemplateLoader taskTemplateLoader;

    public List<TaskTemplate> getTaskTemplates() {
        return taskTemplateLoader.getTaskTemplates();
    }

    @Override
    public Optional<TaskTemplate> fetchByName(String name) {
        Preconditions.checkNotNull(name, "Template name should not be null");
        return Optional.ofNullable(taskTemplateLoader.getTaskTemplateMap().get(name));
    }

}
