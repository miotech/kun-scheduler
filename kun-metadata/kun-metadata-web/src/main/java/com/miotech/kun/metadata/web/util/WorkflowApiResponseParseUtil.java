package com.miotech.kun.metadata.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;

public class WorkflowApiResponseParseUtil {

    private WorkflowApiResponseParseUtil() {
    }

    public static Long parseOperatorIdAfterSearch(String result, String operatorName) {
        JsonNode root = parseJson(result);
        for (JsonNode jsonNode : root) {
            JsonNode id = jsonNode.get(PropKey.ID);
            JsonNode name = jsonNode.get(PropKey.NAME);
            if (id == null || !id.isLong() || name == null || !name.isTextual()) {
                throw new IllegalStateException("attribute `id`|`name` not found");
            }

            if (operatorName.equals(name.textValue())) {
                return id.longValue();
            }
        }

        throw new IllegalArgumentException("Operator Not Found");
    }

    public static Long parseIdAfterCreate(String result) {
        JsonNode root = parseJson(result);
        JsonNode id = root.get(PropKey.ID);
        if (id == null || !id.isLong()) {
            throw new IllegalStateException("attribute `id` not found");
        }

        return id.longValue();
    }

    public static Long parseTaskIdAfterSearch(String result, Long operatorId, String taskName) {
        JsonNode root = parseJson(result);
        for (JsonNode jsonNode : root) {
            JsonNode id = jsonNode.get(PropKey.ID);
            JsonNode name = jsonNode.get(PropKey.NAME);
            JsonNode operatorIdNode = jsonNode.get(PropKey.OPERATOR_ID);
            if (id == null || !id.isLong() || name == null || !name.isTextual() || operatorIdNode == null || !operatorIdNode.isLong()) {
                throw new IllegalStateException("attribute `id`|`name`|`operatorId` not found");
            }

            if (taskName.equals(name.textValue()) && operatorId.equals(operatorIdNode.longValue())) {
                return id.longValue();
            }
        }

        throw new IllegalArgumentException("Task Not Found");
    }

    public static boolean judgeOperatorExists(String result, String operatorName) {
        if (StringUtils.isBlank(result) || StringUtils.isBlank(operatorName)) {
            return false;
        }

        JsonNode root = parseJson(result);
        for (JsonNode jsonNode : root) {
            if (jsonNode == null || jsonNode.isEmpty()) {
                return false;
            }

            JsonNode name = jsonNode.get(PropKey.NAME);
            if (name == null || !name.isTextual()) {
                return false;
            }

            if (operatorName.equals(name.textValue())) {
                return true;
            }
        }

        return false;
    }

    public static boolean judgeTaskExists(String result, String operatorIdStr, String taskName) {
        if (StringUtils.isBlank(result) || StringUtils.isBlank(operatorIdStr) || StringUtils.isBlank(taskName)) {
            return false;
        }

        JsonNode root = parseJson(result);
        for (JsonNode jsonNode : root) {
            if (jsonNode == null || jsonNode.isEmpty()) {
                return false;
            }

            JsonNode name = jsonNode.get(PropKey.NAME);
            JsonNode operatorId = jsonNode.get(PropKey.OPERATOR_ID);
            if (name == null || !name.isTextual() || operatorId == null || !operatorId.isLong()) {
                return false;
            }

            if (taskName.equals(name.textValue()) &&
                    (StringUtils.isNotBlank(operatorIdStr) && Long.parseLong(operatorIdStr) == operatorId.longValue())) {
                return true;
            }
        }

        return false;
    }

    private static JsonNode parseJson(String json) {
        try {
            return JSONUtils.stringToJson(json);
        } catch (JsonProcessingException jsonProcessingException) {
            throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
        }
    }

    public static String parseProcessId(String result) {
        JsonNode root = parseJson(result);
        if (!root.isArray()) {
            throw new IllegalStateException("Run Task Fail");
        }

        return String.valueOf(root.get(0).longValue());
    }

    public static boolean isSuccess(String result) {
        if (StringUtils.isBlank(result)) {
            return false;
        }

        JsonNode root;
        try {
            root = JSONUtils.stringToJson(result);
        } catch (JsonProcessingException jsonProcessingException) {
            throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
        }

        JsonNode code = root.get("code");
        if (code != null && code.isInt() && code.intValue() != 200) {
            return false;
        }

        return true;
    }
}
