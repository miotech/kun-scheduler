package com.miotech.kun.dataplatform.common.datastore.vo;

import com.miotech.kun.dataplatform.common.taskdefinition.vo.TaskDefinitionProps;
import lombok.Data;

import java.util.List;

@Data
public class DatasetVO {

    private final Long datastoreId;

    private final String name;

    private final List<TaskDefinitionProps> taskDefinitions;
}
