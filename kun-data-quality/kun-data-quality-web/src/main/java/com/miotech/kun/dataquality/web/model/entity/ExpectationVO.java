package com.miotech.kun.dataquality.web.model.entity;

import com.miotech.kun.dataquality.web.model.bo.AssertionRequest;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExpectationVO extends ExpectationBasic {

    private MetricsRequest metrics;

    private AssertionRequest assertion;

    private List<DatasetBasic> relatedTables;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private MetricsRequest metrics;
        private AssertionRequest assertion;
        private List<DatasetBasic> relatedTables;
        private Long id;
        private String name;
        private List<String> types;
        private String description;
        private String updater;
        private Long taskId;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private Boolean isPrimary;
        private Boolean isBlocking;

        private Builder() {
        }

        public Builder withMetrics(MetricsRequest metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder withAssertion(AssertionRequest assertion) {
            this.assertion = assertion;
            return this;
        }

        public Builder withRelatedTables(List<DatasetBasic> relatedTables) {
            this.relatedTables = relatedTables;
            return this;
        }

        public Builder withId(Long id) {
            this.id = id;
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

        public Builder withUpdater(String updater) {
            this.updater = updater;
            return this;
        }

        public Builder withTaskId(Long taskId) {
            this.taskId = taskId;
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

        public Builder withIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public Builder withIsBlocking(Boolean isBlocking) {
            this.isBlocking = isBlocking;
            return this;
        }

        public ExpectationVO build() {
            ExpectationVO expectationVO = new ExpectationVO();
            expectationVO.setMetrics(metrics);
            expectationVO.setAssertion(assertion);
            expectationVO.setRelatedTables(relatedTables);
            expectationVO.setId(id);
            expectationVO.setName(name);
            expectationVO.setTypes(types);
            expectationVO.setDescription(description);
            expectationVO.setUpdater(updater);
            expectationVO.setTaskId(taskId);
            expectationVO.setCreateTime(createTime);
            expectationVO.setUpdateTime(updateTime);
            expectationVO.setIsPrimary(isPrimary);
            expectationVO.setIsBlocking(isBlocking);
            return expectationVO;
        }
    }
}
