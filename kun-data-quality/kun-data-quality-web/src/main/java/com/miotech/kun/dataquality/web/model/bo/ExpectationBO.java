package com.miotech.kun.dataquality.web.model.bo;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.*;
import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.core.model.task.CheckType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.miotech.kun.dataquality.core.ExpectationSpec.ExpectationTrigger.DATASET_UPDATED;

@Data
@Builder
public class ExpectationBO {

    private String name;

    private List<String> types;

    private String description;

    private String sql;

    private List<DataQualityRule> validateRules;

    private List<Long> relatedTableIds;

    private Long datasetGid;

    private DatasetBasicInfo datasetBasicInfo;

    private Long taskId;

    private String createUser;

    private OffsetDateTime createTime;

    private String updateUser;

    private OffsetDateTime updateTime;

    private boolean isBlocking;

    public ExpectationSpec convertTo(Long dataSourceId) {
        List<JDBCExpectationAssertion> assertions = validateRules.stream()
                .map(vr -> {
                    JDBCExpectationAssertion.ComparisonOperator comparisonOperator = JDBCExpectationAssertion.ComparisonOperator.convertFrom(vr.getOperator());
                    return new JDBCExpectationAssertion(vr.getField(), comparisonOperator, comparisonOperator.getSymbol(), vr.getExpectedType(), vr.getExpectedValue());
                })
                .collect(Collectors.toList());
        JDBCExpectationMethod method = new JDBCExpectationMethod(this.sql, Lists.newArrayList(assertions));

        Dataset dataset = Dataset.builder()
                .gid(this.datasetGid)
                .dataSource(DataSource.newBuilder().withId(dataSourceId).build())
                .build();
        return ExpectationSpec.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName(this.name)
                .withTypes(this.types)
                .withDescription(this.description)
                .withMethod(method)
                .withTrigger(DATASET_UPDATED)
                .withDataset(dataset)
                .withIsBlocking(this.isBlocking)
                .withCreateTime(this.createTime)
                .withUpdateTime(this.updateTime)
                .withCreateUser(this.createUser)
                .withUpdateUser(this.updateUser)
                .build();
    }

    public CheckType getCheckType() {
        CheckType checkType = CheckType.SKIP;
        if (this.isBlocking()) {
            checkType = CheckType.WAIT_EVENT;
        }
        return checkType;
    }

}
