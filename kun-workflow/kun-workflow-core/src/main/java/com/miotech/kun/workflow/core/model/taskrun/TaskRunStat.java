package com.miotech.kun.workflow.core.model.taskrun;

public class TaskRunStat {
    private final Long id;

    private final Long averageRunningTime;

    private final Long averageQueuingTime;

    public TaskRunStat(Long id, Long averageRunningTime, Long averageQueuingTime) {
        this.id = id;
        this.averageRunningTime = averageRunningTime;
        this.averageQueuingTime = averageQueuingTime;
    }

    public Long getId() {
        return id;
    }

    public Long getAverageRunningTime() {
        return averageRunningTime;
    }

    public Long getAverageQueuingTime() {
        return averageQueuingTime;
    }

    public static TaskRunStatBuilder newBuilder() {
        return new TaskRunStatBuilder();
    }

    public static final class TaskRunStatBuilder {
        private Long id;
        private Long averageRunningTime;
        private Long averageQueuingTime;

        private TaskRunStatBuilder() {
        }

        public static TaskRunStatBuilder aTaskRunStat() {
            return new TaskRunStatBuilder();
        }

        public TaskRunStatBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskRunStatBuilder withAverageRunningTime(Long averageRunningTime) {
            this.averageRunningTime = averageRunningTime;
            return this;
        }

        public TaskRunStatBuilder withAverageQueuingTime(Long averageQueuingTime) {
            this.averageQueuingTime = averageQueuingTime;
            return this;
        }

        public TaskRunStat build() {
            return new TaskRunStat(id, averageRunningTime, averageQueuingTime);
        }
    }
}
