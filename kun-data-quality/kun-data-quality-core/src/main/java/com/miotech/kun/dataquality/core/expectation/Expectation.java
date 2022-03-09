package com.miotech.kun.dataquality.core.expectation;

import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.metrics.Metrics;

import java.time.OffsetDateTime;
import java.util.List;

public class Expectation {

    private final Long expectationId;

    private final String name;

    private final List<String> types;

    private final String description;

    private final ExpectationMethod method;

    private final ExpectationTrigger trigger;

    private final Metrics metrics;

    private final Assertion assertion;

    private final Long taskId;

    private final Dataset dataset;

    private final boolean isBlocking;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;

    private final String createUser;

    private final String updateUser;

    public Expectation(Long expectationId, String name, List<String> types, String description, ExpectationMethod method,
                       ExpectationTrigger trigger, Metrics metrics, Assertion assertion, Long taskId, Dataset dataset,
                       boolean isBlocking, OffsetDateTime createTime, OffsetDateTime updateTime, String createUser, String updateUser) {
        this.expectationId = expectationId;
        this.name = name;
        this.types = types;
        this.description = description;
        this.method = method;
        this.trigger = trigger;
        this.metrics = metrics;
        this.assertion = assertion;
        this.taskId = taskId;
        this.dataset = dataset;
        this.isBlocking = isBlocking;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createUser = createUser;
        this.updateUser = updateUser;
    }

    public Long getExpectationId() {
        return expectationId;
    }

    public String getName() {
        return name;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getDescription() {
        return description;
    }

    public ExpectationMethod getMethod() {
        return method;
    }

    public ExpectationTrigger getTrigger() {
        return trigger;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Assertion getAssertion() {
        return assertion;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public enum ExpectationTrigger {
        SCHEDULED   // 周期调度触发
        , DATASET_UPDATED   // 关联的数据集更新时触发
        ;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withExpectationId(this.expectationId)
                .withName(this.name)
                .withTypes(this.types)
                .withDescription(this.description)
                .withMethod(this.method)
                .withTrigger(this.trigger)
                .withTaskId(this.taskId)
                .withDataset(this.dataset)
                .withIsBlocking(this.isBlocking)
                .withCreateTime(this.createTime)
                .withUpdateTime(this.updateTime)
                .withCreateUser(this.createUser)
                .withUpdateUser(this.updateUser);
    }

    public static final class Builder {
        private Long expectationId;
        private String name;
        private List<String> types;
        private String description;
        private ExpectationMethod method;
        private ExpectationTrigger trigger;
        private Metrics metrics;
        private Assertion assertion;
        private Long taskId;
        private Dataset dataset;
        private boolean isBlocking;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private String createUser;
        private String updateUser;

        private Builder() {
        }

        public Builder withExpectationId(Long expectationId) {
            this.expectationId = expectationId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTypes(List<String> types) {
            this.types = types;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withMethod(ExpectationMethod method) {
            this.method = method;
            return this;
        }

        public Builder withTrigger(ExpectationTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder withMetrics(Metrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder withAssertion(Assertion assertion) {
            this.assertion = assertion;
            return this;
        }

        public Builder withTaskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder withDataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public Builder withIsBlocking(boolean isBlocking) {
            this.isBlocking = isBlocking;
            return this;
        }

        public Builder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withCreateUser(String createUser) {
            this.createUser = createUser;
            return this;
        }

        public Builder withUpdateUser(String updateUser) {
            this.updateUser = updateUser;
            return this;
        }

        public Expectation build() {
            return new Expectation(expectationId, name, types, description, method, trigger, metrics, assertion,
                    taskId, dataset, isBlocking, createTime, updateTime, createUser, updateUser);
        }
    }
}
