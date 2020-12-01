package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.Preconditions;
import com.miotech.kun.commons.db.sql.BaseSortField;
import com.miotech.kun.commons.db.sql.SortOrder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = TaskDefinitionViewSearchParams.TaskDefinitionViewSearchParamsBuilder.class)
public class TaskDefinitionViewSearchParams {
    String keyword;
    Integer pageNum;
    Integer pageSize;
    Long creator;
    List<Long> taskDefinitionIds;
    SortOrder sortOrder;
    SortKey sortKey;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TaskDefinitionViewSearchParamsBuilder {}

    public enum SortKey implements BaseSortField {
        ID("id"),
        NAME("name"),
        CREATE_TIME("create_time"),
        UPDATE_TIME("update_time");

        private final String fieldColumnName;

        @Override
        public String getFieldColumnName() {
            return fieldColumnName;
        }

        SortKey(String columnName) {
            this.fieldColumnName = columnName;
        }

        public static SortKey from(String buildString) {
            Preconditions.checkNotNull(buildString);
            switch (buildString.toLowerCase()) {
                case "id":
                    return ID;
                case "name":
                    return NAME;
                case "createtime":
                    return CREATE_TIME;
                case "updatetime":
                    return UPDATE_TIME;
                default:
                    throw new IllegalArgumentException(String.format(
                            "Unknown sort key: %s. Allowed keys: id, name, createtime, updatetime",
                            buildString
                    ));
            }
        }
    }
}
