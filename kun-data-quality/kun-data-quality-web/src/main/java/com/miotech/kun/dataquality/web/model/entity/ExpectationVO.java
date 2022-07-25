package com.miotech.kun.dataquality.web.model.entity;

import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExpectationVO extends ExpectationBasic {

    private String granularity = Metrics.Granularity.CUSTOM.name();

    private String templateName;

    private Map<String, Object> payload;

    private Long datasetGid;

    private List<DatasetBasic> relatedTables;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String granularity = Metrics.Granularity.CUSTOM.name();
        private String templateName;
        private Map<String, Object> payload;
        private Long datasetGid;
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
        private CaseType caseType;

        private Builder() {
        }

        public static Builder anExpectationVO() {
            return new Builder();
        }

        public Builder withGranularity(String granularity) {
            this.granularity = granularity;
            return this;
        }

        public Builder withTemplateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder withPayload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder withDatasetGid(Long datasetGid) {
            this.datasetGid = datasetGid;
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

        public Builder withCaseType(CaseType caseType) {
            this.caseType = caseType;
            return this;
        }

        public ExpectationVO build() {
            ExpectationVO expectationVO = new ExpectationVO();
            expectationVO.setGranularity(granularity);
            expectationVO.setTemplateName(templateName);
            expectationVO.setPayload(payload);
            expectationVO.setDatasetGid(datasetGid);
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
            expectationVO.setCaseType(caseType);
            return expectationVO;
        }
    }
}
