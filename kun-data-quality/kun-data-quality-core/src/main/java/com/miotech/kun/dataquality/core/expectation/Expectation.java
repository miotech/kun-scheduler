package com.miotech.kun.dataquality.core.expectation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class Expectation {

    private Long expectationId;

    private String name;

    private List<String> types;

    private String description;

    private String granularity;

    private ExpectationTemplate template;

    private Map<String, Object> payload;

    private ExpectationTrigger trigger;

    private Long taskId;

    private Dataset dataset;

    private CaseType caseType;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;

    private String createUser;

    private String updateUser;

    public Expectation() {
    }

    public Long getExpectationId() {
        return expectationId;
    }

    public void setExpectationId(Long expectationId) {
        this.expectationId = expectationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public ExpectationTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ExpectationTemplate template) {
        this.template = template;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public ExpectationTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(ExpectationTrigger trigger) {
        this.trigger = trigger;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseType caseType) {
        this.caseType = caseType;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(OffsetDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
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
                .withGranularity(this.granularity)
                .withTemplate(this.template)
                .withPayload(this.payload)
                .withTrigger(this.trigger)
                .withTaskId(this.taskId)
                .withDataset(this.dataset)
                .withCaseType(this.caseType)
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
        private String granularity;
        private ExpectationTemplate template;
        private Map<String, Object> payload;
        private ExpectationTrigger trigger;
        private Long taskId;
        private Dataset dataset;
        private CaseType caseType;
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

        public Builder withGranularity(String granularity) {
            this.granularity = granularity;
            return this;
        }

        public Builder withTemplate(ExpectationTemplate template) {
            this.template = template;
            return this;
        }

        public Builder withPayload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder withTrigger(ExpectationTrigger trigger) {
            this.trigger = trigger;
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

        public Builder withCaseType(CaseType caseType) {
            this.caseType = caseType;
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
            Expectation expectation = new Expectation();
            expectation.setExpectationId(expectationId);
            expectation.setName(name);
            expectation.setTypes(types);
            expectation.setDescription(description);
            expectation.setGranularity(granularity);
            expectation.setTemplate(template);
            expectation.setPayload(payload);
            expectation.setTrigger(trigger);
            expectation.setTaskId(taskId);
            expectation.setDataset(dataset);
            expectation.setCaseType(caseType);
            expectation.setCreateTime(createTime);
            expectation.setUpdateTime(updateTime);
            expectation.setCreateUser(createUser);
            expectation.setUpdateUser(updateUser);
            return expectation;
        }
    }
}
