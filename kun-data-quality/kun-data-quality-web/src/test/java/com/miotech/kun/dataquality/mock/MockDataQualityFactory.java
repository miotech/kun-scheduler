package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.web.model.TemplateType;
import com.miotech.kun.dataquality.web.model.bo.DataQualityRequest;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MockDataQualityFactory {

    public static DataQualityRequest createRequest() {
        return createRequestWithRelatedTable(null, null);
    }

    public static DataQualityRequest createRequestWithRelatedTable(List<Long> relatedTableIds, Long primaryDatasetGid) {
        String dimension = TemplateType.CUSTOMIZE.name();
        String sql = "";
        JSONObject dimensionConfig = new JSONObject();
        dimensionConfig.put("sql", sql);
        return createRequest(dimension, dimensionConfig, relatedTableIds, primaryDatasetGid);
    }

    public static DataQualityRequest createRequest(String dimension, JSONObject dimensionConfig, List<Long> relatedTableIds, Long primaryDatasetGid) {
        Long taskId = WorkflowIdGenerator.nextTaskId();
        DataQualityRequest dataQualityRequest = new DataQualityRequest();
        dataQualityRequest.setTaskId(taskId);
        dataQualityRequest.setName("case_" + taskId);
        dataQualityRequest.setIsBlocking(true);
        dataQualityRequest.setDimension(dimension);
        dataQualityRequest.setDimensionConfig(dimensionConfig);
        dataQualityRequest.setCreateTime(System.currentTimeMillis());
        dataQualityRequest.setUpdateTime(System.currentTimeMillis());
        dataQualityRequest.setDescription("");
        dataQualityRequest.setCreateUser("create_user");
        dataQualityRequest.setUpdateUser("update_user");
        dataQualityRequest.setValidateRules(new ArrayList<>());
        dataQualityRequest.setRelatedTableIds(relatedTableIds);
        dataQualityRequest.setPrimaryDatasetGid(primaryDatasetGid);
        return dataQualityRequest;
    }
}
