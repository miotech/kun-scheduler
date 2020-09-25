package com.miotech.kun.dataplatform.common.taskdefinition.vo;

import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class TaskRunRequest {
    private JSONObject parameters = new JSONObject();

    private JSONObject variables = new JSONObject();
}
