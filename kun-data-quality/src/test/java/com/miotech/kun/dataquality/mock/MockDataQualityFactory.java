package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.model.TemplateType;
import com.miotech.kun.dataquality.model.bo.DataQualityRequest;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class MockDataQualityFactory {

    public static DataQualityRequest createRequest(){
        String dimension = TemplateType.CUSTOMIZE.name();
        String sql = "";
        JSONObject dimensionConfig = new JSONObject();
        dimensionConfig.put("sql",sql);
        return createRequest(dimension,dimensionConfig);
    }

    public static DataQualityRequest createRequest(String dimension, JSONObject dimensionConfig){
        Long taskId = WorkflowIdGenerator.nextTaskId();
        DataQualityRequest dataQualityRequest = new DataQualityRequest();
        dataQualityRequest.setTaskId(taskId);
        dataQualityRequest.setName("case_" + taskId);
        dataQualityRequest.setIsBlock(false);
        dataQualityRequest.setDimension(dimension);
        dataQualityRequest.setDimensionConfig(dimensionConfig);
        dataQualityRequest.setCreateTime(System.currentTimeMillis());
        dataQualityRequest.setUpdateTime(System.currentTimeMillis());
        dataQualityRequest.setDescription("");
        dataQualityRequest.setCreateUser("create_user");
        dataQualityRequest.setUpdateUser("update_user");
        dataQualityRequest.setValidateRules(new ArrayList<>());
        return dataQualityRequest;
    }
}
