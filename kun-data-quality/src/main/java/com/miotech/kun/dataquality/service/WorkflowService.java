package com.miotech.kun.dataquality.service;

import com.miotech.kun.dataquality.DataQualityConfiguration;
import com.miotech.kun.dataquality.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.WorkflowApiException;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Service
@Slf4j
public class WorkflowService {

    @Autowired
    WorkflowClient workflowClient;

    @Autowired
    DataQualityService dataQualityService;

    @Autowired
    Operator operator;

    @Autowired
    WorkflowUtils workflowUtils;

    @Value("${data-quality.workflow.task.cron:0 0 0 * * ?}")
    String cronExpression;

    @Value("${workflow.enable:true}")
    Boolean workflowEnable;

    @Autowired
    private OperatorUpload operatorUpload;

    @PostConstruct
    public void init() {
        if (workflowEnable) {
            operatorUpload.autoUpload();
            workflowClient.saveOperator(this.operator.getName(), this.operator);
        }
    }

    public Long createTask(Long caseId) {

        Task task = buildTask(caseId);

        Task savedTask = workflowClient.createTask(task);
        dataQualityService.saveTaskId(caseId, savedTask.getId());
        return savedTask.getId();
    }

    public Long executeTask(Long caseId) {

        Long taskId = dataQualityService.getLatestTaskId(caseId);
        if (taskId == null || taskId.equals(0L)) {
            taskId = createTask(caseId);
        } else {
            try {
                workflowClient.getTask(taskId);
            } catch (Exception e) {
                taskId = createTask(caseId);
            }
        }

        TaskRun taskRun = workflowClient.executeTask(taskId, null);
        log.info("Execute task " + taskId + " taskRun " + taskRun.getId());

        return taskId;
    }

    public void deleteTaskByCase(Long caseId) {
        Long taskId = dataQualityService.getLatestTaskId(caseId);
        try {
            if (taskId != null) {
                workflowClient.getTask(taskId);
            }
        } catch (WorkflowApiException e) {
            dataQualityService.saveTaskId(caseId, null);
            return;
        }
        deleteTask(taskId);
    }

    public void deleteTask(Long taskId) {
        if (taskId != null && !taskId.equals(0L)) {
            workflowClient.deleteTask(taskId);
        }
    }

    private Task buildTask(Long caseId) {
        Operator savedOperator = workflowClient.saveOperator(this.operator.getName(), this.operator);

        String caseName = dataQualityService.getCaseBasic(caseId).getName();
        return Task.newBuilder()
                .withName(DataQualityConfiguration.WORKFLOW_TASK_NAME_PREFIX + caseId + "_" + caseName)
                .withDescription("")
                .withConfig(workflowUtils.getTaskConfig(caseId))
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, cronExpression))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withOperatorId(savedOperator.getId())
                .build();
    }

}
