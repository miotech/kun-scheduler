package com.miotech.kun.workflow.core.model.worker;

import java.util.Objects;

public class WorkerInstance{
    private final long taskAttemptId;
    private final String workerId;
    private final String nameSpace;
    private final WorkerInstanceEnv env;

    public WorkerInstance(long taskAttemptId, String workerId, String nameSpace, WorkerInstanceEnv env) {
        this.taskAttemptId = taskAttemptId;
        this.workerId = workerId;
        this.nameSpace = nameSpace;
        this.env = env;
    }

    public long getTaskAttemptId() {
        return taskAttemptId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getNameSpace(){
        return nameSpace;
    }

    public WorkerInstanceBuilder cloneBuilder(){
        return newBuilder()
                .withWorkerId(workerId)
                .withTaskAttemptId(taskAttemptId)
                .withNameSpace(nameSpace)
                .withEnv(env);
    }

    public WorkerInstanceEnv getEnv() {
        return env;
    }

    public static WorkerInstanceBuilder newBuilder(){
        return new WorkerInstanceBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerInstance)) return false;
        WorkerInstance that = (WorkerInstance) o;
        return getTaskAttemptId() == that.getTaskAttemptId() &&
                Objects.equals(getWorkerId(), that.getWorkerId()) &&
                getNameSpace().equals(that.getNameSpace()) &&
                getEnv() == that.getEnv();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskAttemptId(), getWorkerId(), getNameSpace(), getEnv());
    }

    @Override
    public String toString() {
        return "WorkerInstance{" +
                "taskAttemptId=" + taskAttemptId +
                ", workerId='" + workerId + '\'' +
                ", nameSpace='" + nameSpace + '\'' +
                ", env=" + env +
                '}';
    }

    public static final class WorkerInstanceBuilder {
        private long taskAttemptId;
        private String workerId;;
        private String nameSpace;
        private WorkerInstanceEnv env;

        private WorkerInstanceBuilder() {
        }

        public static WorkerInstanceBuilder aWorkerInstance() {
            return new WorkerInstanceBuilder();
        }

        public WorkerInstanceBuilder withTaskAttemptId(long taskAttemptId) {
            this.taskAttemptId = taskAttemptId;
            return this;
        }

        public WorkerInstanceBuilder withWorkerId(String workerId) {
            this.workerId = workerId;
            return this;
        }

        public WorkerInstanceBuilder withEnv(WorkerInstanceEnv env) {
            this.env = env;
            return this;
        }
        public WorkerInstanceBuilder withNameSpace(String nameSpace){
            this.nameSpace = nameSpace;
            return this;
        }

        public WorkerInstance build() {
            return new WorkerInstance(taskAttemptId, workerId, nameSpace, env);
        }
    }
}
