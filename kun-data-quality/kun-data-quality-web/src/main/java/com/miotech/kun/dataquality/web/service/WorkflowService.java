package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.common.constant.DataQualityConstant;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import com.miotech.kun.dataquality.web.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.WorkflowApiException;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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
    Operator operator;

    @Autowired
    WorkflowUtils workflowUtils;

    @Value("${data-quality.workflow.task.cron:0 0 0 * * ?}")
    String cronExpression;

    @Value("${workflow.enabled:true}")
    Boolean workflowEnable;

    @Autowired
    MetadataClient metadataClient;

    @Autowired
    private OperatorUpload operatorUpload;

    @Autowired
    private ExpectationService expectationService;

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
        expectationService.updateTaskId(caseId, savedTask.getId());
        return savedTask.getId();
    }

    public TaskRun executeTask(Long caseId) {

        Long taskId = expectationService.getTaskId(caseId);
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

        return taskRun;
    }

    public List<Long> executeTasks(List<Long> caseIds){
        List<Long> taskRunIdList = new ArrayList<>();
        for(Long id: caseIds){
            TaskRun taskRun = executeTask(id);
            taskRunIdList.add(taskRun.getId());
        }
        return taskRunIdList;
    }

    public void deleteTaskByCase(Long caseId) {
        Long taskId = expectationService.getTaskId(caseId);
        try {
            if (taskId != null) {
                workflowClient.getTask(taskId);
            }
        } catch (WorkflowApiException e) {
            return;
        }

        deleteTask(taskId);
    }

    public void deleteTask(Long taskId) {
        if (taskId != null && !taskId.equals(0L)) {
            workflowClient.deleteTask(taskId);
        }
    }

    public void updateUpstreamTaskCheckType(Long dataSetId, CheckType checkType){
        List<Long> upstreamTaskIds = metadataClient.fetchUpstreamTaskIds(dataSetId);
        for (Long upstreamTaskId : upstreamTaskIds) {
            Task task = Task.newBuilder()
                    .withId(upstreamTaskId)
                    .withCheckType(checkType.name())
                    .build();
            workflowClient.saveTask(task, null);
        }
    }

    private Task buildTask(Long caseId) {
        Operator savedOperator = workflowClient.saveOperator(this.operator.getName(), this.operator);

        String caseName = expectationService.fetchById(caseId).getName();
        return Task.newBuilder()
                .withName(DataQualityConstant.WORKFLOW_TASK_NAME_PREFIX + caseId + "_" + caseName)
                .withDescription("")
                .withConfig(workflowUtils.getTaskConfig(caseId))
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withOperatorId(savedOperator.getId())
                .build();
    }

}
