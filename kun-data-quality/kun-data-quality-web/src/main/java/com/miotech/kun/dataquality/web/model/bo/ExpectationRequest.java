package com.miotech.kun.dataquality.web.model.bo;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.core.model.task.CheckType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

import static com.miotech.kun.dataquality.core.expectation.Expectation.ExpectationTrigger.DATASET_UPDATED;

@Data
@Builder
public class ExpectationRequest {

    private String name;

    private List<String> types;

    private String description;

    private MetricsRequest metrics;

    private AssertionRequest assertion;

    private List<Long> relatedDatasetGids;

    private CaseType caseType;

    public Expectation convertTo(Long dataSourceId, String currentUsername) {
        Dataset dataset = Dataset.builder()
                .gid(this.metrics.getDatasetGid())
                .dataSource(DataSource.newBuilder().withId(dataSourceId).build())
                .build();
        OffsetDateTime now = DateTimeUtils.now();
        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName(this.name)
                .withTypes(this.types)
                .withDescription(this.description)
                .withMethod(null)
                .withMetrics(this.metrics.convertTo(this.name, this.description, dataset))
                .withAssertion(this.assertion.convertTo())
                .withTrigger(DATASET_UPDATED)
                .withDataset(dataset)
                .withCaseType(this.caseType)
                .withCreateTime(now)
                .withUpdateTime(now)
                .withCreateUser(currentUsername)
                .withUpdateUser(currentUsername)
                .build();
    }

    public CheckType getCheckType() {
        switch (caseType){
            case BLOCK:
                return CheckType.WAIT_EVENT_PASS;
            case FINAL_SUCCESS:
                return CheckType.WAIT_EVENT;
            default:
                return CheckType.SKIP;
        }
    }

}
