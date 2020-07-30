package com.miotech.kun.dataplatform.common.tasktemplate.dao;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TaskTemplateLoader {

    private final WorkflowClient workflowClient;
    private final Map<String, TaskTemplate> taskTemplateMap;
    private final List<TaskTemplate> taskTemplates;
    private final ResourcePatternResolver resourceResolver;

    public List<TaskTemplate> getTaskTemplates() {
        return taskTemplates;
    }

    public TaskTemplateLoader(@Autowired WorkflowClient workflowClient,
                              @Autowired ResourcePatternResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        this.workflowClient = workflowClient;
        this.taskTemplates = loadTaskTemplates();
        taskTemplateMap = taskTemplates.stream()
                    .collect(Collectors.toMap(TaskTemplate::getName, Function.identity()));
    }

    public Map<String, TaskTemplate> getTaskTemplateMap() {
        return taskTemplateMap;
    }

    private List<TaskTemplate> loadTaskTemplates() {
        Resource[] resources = new Resource[]{};
        try {
            resources = resourceResolver.getResources("classpath:templates/*.json");
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        if ( resources.length == 0) {
            log.warn("No TaskTemplate found in resource path, could not run any tasks without task templates");
            return Collections.emptyList();
        } else {
            List<TaskTemplate> result = new ArrayList<>();
            for (Resource f: resources) {
                log.info("Parse TaskTemplate from {}", f.getFilename());

                try (InputStream fileInputStream = f.getInputStream()) {
                    TaskTemplate taskTemplate = JSONUtils.jsonToObject(fileInputStream, TaskTemplate.class);
                    String operatorName = taskTemplate.getOperator().getName();
                    Operator updated = workflowClient.saveOperator(
                            operatorName,
                            taskTemplate.getOperator());
                    log.info("Load and update operator \"{}\"-\"{}\"", updated.getName(), updated.getId());
                    if (CollectionUtils.isEmpty(updated.getConfigDef())) {
                        workflowClient.updateOperatorJar(operatorName, new File(f.getFile().getParent() +  "/" + taskTemplate.getJarPath()));
                        updated = workflowClient.getOperator(operatorName).get();
                    }
                    Preconditions.checkNotNull(updated.getConfigDef(), "Operator ConfigDef should not be null");
                    result.add(taskTemplate
                            .cloneBuilder()
                            .withOperator(updated)
                            .build());
                } catch (IOException e) {
                    log.error("Error fetch task template from {}", f.getFilename(), e);
                    throw ExceptionUtils.wrapIfChecked(e);
                }
            }
            return result;
        }
    }

}
