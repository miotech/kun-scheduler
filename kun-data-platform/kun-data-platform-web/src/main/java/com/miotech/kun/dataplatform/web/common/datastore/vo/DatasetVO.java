package com.miotech.kun.dataplatform.web.common.datastore.vo;

import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.TaskDefinitionProps;
import lombok.Data;

import java.util.List;

@Data
public class DatasetVO {

    private final Long datastoreId;

    private final String name;

    private final List<TaskDefinitionProps> taskDefinitions;
}
