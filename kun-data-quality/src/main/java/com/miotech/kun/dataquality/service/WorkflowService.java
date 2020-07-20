package com.miotech.kun.dataquality.service;

import com.miotech.kun.dataquality.DataQualityConfiguration;
import com.miotech.kun.dataquality.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${data-quality.workflow.task.cron}")
    String cronExpression;

    public Long createTask(Long caseId) {

        Task task = buildTask(caseId);

        Task savedTask = workflowClient.createTask(task);
        return savedTask.getId();
    }

    public Long executeTask(Long caseId) {

        Long taskId = dataQualityService.getLatestTaskId(caseId);
        if (taskId == null || taskId.equals(0L)) {
            taskId = createTask(caseId);
        }

        TaskRun taskRun = workflowClient.executeTask(taskId, null);
        log.info("Execute task " + taskId + " taskRun " + taskRun.getId());

        return taskId;
    }

    public void deleteTask(Long caseId) {
        Long taskId = dataQualityService.getLatestTaskId(caseId);
        if (taskId != null && !taskId.equals(0L)) {
            workflowClient.deleteTask(taskId);
        }
    }

    private Task buildTask(Long caseId) {
        Operator savedOperator = workflowClient.saveOperator(this.operator.getName(), this.operator);

        String caseName = dataQualityService.getCaseBasic(caseId).getName();
        return Task.newBuilder()
                .withName(DataQualityConfiguration.WORKFLOW_TASK_NAME_PREFIX + caseName)
                .withDescription("")
                .withArguments(workflowUtils.getInitParams(String.valueOf(caseId)))
                .withVariableDefs(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, cronExpression))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withOperatorId(savedOperator.getId())
                .build();
    }

}
