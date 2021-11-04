package com.miotech.kun.monitor.facade.model.sla;

public class TaskDefinitionNode {

    public static TaskDefinitionNode from(Long taskDefId, String name, Integer level, Integer deadline, Long workflowTaskId, Integer runTime) {
        TaskDefinitionNode taskDefinitionNode = new TaskDefinitionNode();
        taskDefinitionNode.setId(taskDefId);
        taskDefinitionNode.setName(name);
        taskDefinitionNode.setLevel(level);
        taskDefinitionNode.setDeadline(deadline);
        taskDefinitionNode.setWorkflowTaskId(workflowTaskId);
        taskDefinitionNode.setRunTime(runTime);
        return taskDefinitionNode;
    }

    private Long id;

    private String name;

    private Integer level;

    private Integer deadline;

    private Long workflowTaskId;

    private Integer runTime;

    public TaskDefinitionNode() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getDeadline() {
        return deadline;
    }

    public void setDeadline(Integer deadline) {
        this.deadline = deadline;
    }

    public Long getWorkflowTaskId() {
        return workflowTaskId;
    }

    public void setWorkflowTaskId(Long workflowTaskId) {
        this.workflowTaskId = workflowTaskId;
    }

    public Integer getRunTime() {
        return runTime;
    }

    public void setRunTime(Integer runTime) {
        this.runTime = runTime;
    }

    public static enum Relationship {
        OUTPUT, INPUT
    }

}
