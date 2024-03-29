package com.miotech.kun.dataplatform.web.common.tasktemplate.dao;

import com.miotech.kun.dataplatform.web.model.tasktemplate.TaskTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskTemplateDao {

    List<TaskTemplate> getTaskTemplates();

    Optional<TaskTemplate> fetchByName(String name);

    TaskTemplate create(TaskTemplate template);

    TaskTemplate update(TaskTemplate template);

}
