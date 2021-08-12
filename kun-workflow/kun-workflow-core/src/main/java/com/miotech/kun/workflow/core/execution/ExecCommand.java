package com.miotech.kun.workflow.core.execution;

public class ExecCommand {
    private String jarPath;
    private String className;
    private Config config;
    private String logPath;
    private String registerUrl;
    private Long taskRunId;
    private Long taskAttemptId;
    private String queueName;
    private Boolean keepAlive;

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public void setTaskRunId(Long taskRunId) {
        this.taskRunId = taskRunId;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public void setTaskAttemptId(Long taskAttemptId) {
        this.taskAttemptId = taskAttemptId;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public void setRegisterUrl(String registerUrl) {
        this.registerUrl = registerUrl;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public String toString() {
        return "ExecCommand{" +
                "jarPath='" + jarPath + '\'' +
                ", className='" + className + '\'' +
                ", config=" + config +
                ", logPath='" + logPath + '\'' +
                ", registerUrl='" + registerUrl + '\'' +
                ", taskRunId=" + taskRunId +
                ", taskAttemptId=" + taskAttemptId +
                ", queueName='" + queueName + '\'' +
                ", keepAlive=" + keepAlive +
                '}';
    }
}
