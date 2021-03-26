package com.miotech.kun.workflow.core.model.worker;

public class WorkerInstance{
    private final long taskAttemptId;
    private final String workerId;
    private final WorkerInstanceEnv env;

    public WorkerInstance(long taskAttemptId, String workerId,WorkerInstanceEnv env) {
        this.taskAttemptId = taskAttemptId;
        this.workerId = workerId;
        this.env = env;
    }

    public long getTaskAttemptId() {
        return taskAttemptId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public WorkerInstanceBuilder cloneBuilder(){
        return newBuilder()
                .withWorkerId(workerId)
                .withTaskAttemptId(taskAttemptId)
                .withEnv(env);
    }

    public WorkerInstanceEnv getEnv() {
        return env;
    }

    public static WorkerInstanceBuilder newBuilder(){
        return new WorkerInstanceBuilder();
    }


    public static final class WorkerInstanceBuilder {
        private long taskAttemptId;
        private String workerId;
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

        public WorkerInstance build() {
            return new WorkerInstance(taskAttemptId, workerId, env);
        }
    }
}
