package com.miotech.kun.dataplatform.common.tasktemplate.service;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.common.tasktemplate.dao.TaskTemplateDao;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import com.miotech.kun.workflow.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TaskTemplateLoader {

    private final WorkflowClient workflowClient;
    private final List<TaskTemplate> taskTemplates;
    private final ResourcePatternResolver resourceResolver;
    private final TaskTemplateDao taskTemplateDao;
    private final OperatorUpload operatorUpload;

    public TaskTemplateLoader(@Autowired WorkflowClient workflowClient,
                              @Autowired TaskTemplateDao taskTemplateDao,
                              @Autowired ResourcePatternResolver resourceResolver,
                              @Autowired OperatorUpload operatorUpload) {
        this.resourceResolver = resourceResolver;
        this.taskTemplateDao = taskTemplateDao;
        this.workflowClient = workflowClient;
        this.operatorUpload = operatorUpload;
        this.taskTemplates = loadTaskTemplates();
    }

    @PostConstruct
    public void persistTemplates() {
        this.taskTemplates.forEach(x -> {
            Optional<TaskTemplate> templateOptional =
                    taskTemplateDao.fetchByName(x.getName());
            if (templateOptional.isPresent()) {
                taskTemplateDao.update(x);
            } else {
                taskTemplateDao.create(x);
            }
        });
    }

    public List<TaskTemplate> getTaskTemplates() {
        return taskTemplates;
    }

    private List<TaskTemplate> loadTaskTemplates() {
//        try {
//            List<Operator> operatorList = operatorUpload.autoUpload();
//        }catch (Exception e){
//            log.error("auto upload operator failed");
//            throw ExceptionUtils.wrapIfChecked(e);
//        }
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
                    if (CollectionUtils.isEmpty(updated.getConfigDef())
                            && StringUtils.isNoneEmpty(taskTemplate.getJarPath())) {
                        File resourceFile = new File(f.getFile().getParent() +  "/" + taskTemplate.getJarPath());
                        if ( resourceFile.exists()
                                && resourceFile.getPath().endsWith("jar")) {
                            workflowClient.updateOperatorJar(operatorName, resourceFile);
                            updated = workflowClient.getOperator(operatorName).get();
                        }
                    }
                    if (updated.getConfigDef() == null) {
                        log.warn("Operator \"{}\" ConfigDef is `null`, might not be initialized", updated.getName());
                    }

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
