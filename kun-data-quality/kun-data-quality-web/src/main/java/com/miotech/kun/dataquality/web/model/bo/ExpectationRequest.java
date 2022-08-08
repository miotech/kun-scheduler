package com.miotech.kun.dataquality.web.model.bo;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.core.model.task.CheckType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.miotech.kun.dataquality.core.expectation.Expectation.ExpectationTrigger.DATASET_UPDATED;

@Data
@Builder
public class ExpectationRequest {

    private String name;

    private List<String> types;

    private String description;

    private String granularity;

    private String templateName;

    private Map<String, Object> payload;

    private Long datasetGid;

    private List<Long> relatedDatasetGids;

    private CaseType caseType;

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

    public Expectation convertTo(Long dataSourceId, String currentUsername) {
        Dataset dataset = Dataset.builder()
                .gid(this.getDatasetGid())
                .dataSource(DataSource.newBuilder().withId(dataSourceId).build())
                .build();
        OffsetDateTime now = DateTimeUtils.now();

        return Expectation.newBuilder()
                .withExpectationId(IdGenerator.getInstance().nextId())
                .withName(this.name)
                .withTypes(this.types)
                .withDescription(this.description)
                .withGranularity(this.granularity)
                .withTemplate(ExpectationTemplate.newBuilder().withName(this.templateName).build())
                .withPayload(this.getPayload())
                .withTrigger(DATASET_UPDATED)
                .withDataset(dataset)
                .withCaseType(this.caseType)
                .withCreateTime(now)
                .withUpdateTime(now)
                .withCreateUser(currentUsername)
                .withUpdateUser(currentUsername)
                .build();
    }

}
