package com.miotech.kun.workflow.facade;

import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkflowServiceFacade {
    public Operator saveOperator(String name,Operator operator);

    public Operator updateOperator(Long operatorId,Operator operator);

    public List<Operator> getOperators();

    public void uploadOperatorJar(Long operatorId, File jarFile);

    public Optional<Operator> getOperator(String operatorName);

    public Optional<Task> getTask(String taskName);

    public Task createTask(Task task);

    public TaskRun executeTask(Long taskId, Map<String, Object> taskConfig);

    public TaskRun getTaskRun(Long taskRunId);

}
