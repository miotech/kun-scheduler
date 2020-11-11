package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class HeartBeatMessage implements Serializable {
    private Long workerId;
    private String workerUrl;//worker的Rpc地址
    private Long taskAttemptId;
    private Long taskRunId;
    private TaskRunStatus taskRunStatus;
    private Integer port;
    private Integer ip;
    private int timeoutTimes = 0;
    private OffsetDateTime lastHeartBeatTime;

    private static final long serialVersionUID = 1603850205000l;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getIp() {
        return ip;
    }

    public void setIp(Integer ip) {
        this.ip = ip;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getWorkerUrl() {
        return workerUrl;
    }

    public void setWorkerUrl(String workerUrl) {
        this.workerUrl = workerUrl;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public void setTaskAttemptId(Long taskAttemptId) {
        this.taskAttemptId = taskAttemptId;
    }

    public int getTimeoutTimes() {
        return timeoutTimes;
    }

    public void setTimeoutTimes(int timeoutTimes) {
        this.timeoutTimes = timeoutTimes;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public void setTaskRunId(Long taskRunId) {
        this.taskRunId = taskRunId;
    }

    public OffsetDateTime getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public TaskRunStatus getTaskRunStatus() {
        return taskRunStatus;
    }

    public void setTaskRunStatus(TaskRunStatus taskRunStatus) {
        this.taskRunStatus = taskRunStatus;
    }

    public void setLastHeartBeatTime(OffsetDateTime lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    @Override
    public String toString() {
        return "HeartBeatMessage{" +
                "workerId=" + workerId +
                ", workerUrl='" + workerUrl + '\'' +
                ", taskAttemptId=" + taskAttemptId +
                ", taskRunId=" + taskRunId +
                ", taskRunStatus=" + taskRunStatus +
                ", port=" + port +
                ", ip=" + ip +
                ", timeoutTimes=" + timeoutTimes +
                ", lastHeartBeatTime=" + lastHeartBeatTime +
                '}';
    }
}
