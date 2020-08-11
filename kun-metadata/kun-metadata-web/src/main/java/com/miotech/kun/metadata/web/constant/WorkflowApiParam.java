package com.miotech.kun.metadata.web.constant;

public class WorkflowApiParam {

    private WorkflowApiParam() {
    }

    public static final String OPERATOR_NAME_REFRESH = "DataBuilderOperatorManualRefresh";
    public static final String OPERATOR_NAME_BUILD_ALL = "DataBuilderOperatorAutoRefresh";
    public static final String TASK_NAME_REFRESH = "ManualRefresh";
    public static final String TASK_NAME_BUILD_ALL = "AutoRefresh";
    public static final String PACKAGE_PATH_REFRESH = "file:/server/lib/kun-metadata-databuilder-1.0.jar";
    public static final String PACKAGE_PATH_BUILD_ALL = "file:/server/lib/kun-metadata-databuilder-schema.jar";
    public static final String CLASS_NAME = "com.miotech.kun.metadata.databuilder.schedule.DataBuilderOperator";

}
