package com.miotech.kun.workflow.executor.local.model;

public class LocalProcessParams {

    private  String queueName;
    private  Long taskAttemptId;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public void setTaskAttemptId(Long taskAttemptId) {
        this.taskAttemptId = taskAttemptId;
    }
}
