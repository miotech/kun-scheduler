package com.miotech.kun.metadata.web.constant;

public class WorkflowApiParam {

    private WorkflowApiParam() {
    }

    public static final String OPERATOR_NAME_REFRESH = "Metadata DataBuilder Operator(Refresh)";
    public static final String OPERATOR_NAME_BUILD_ALL = "Metadata DataBuilder Operator(Build All)";
    public static final String TASK_NAME_REFRESH = "Metadata DataBuilder Task(Refresh)";
    public static final String TASK_NAME_BUILD_ALL = "Metadata DataBuilder Task(Build All)";
    public static final String OPERATORS = "operators";
    public static final String TASKS = "tasks";
    public static final String TASKS_RUN = "tasks/_run";
    public static final String PACKAGE_PATH_REFRESH = "file:/server/lib/kun-metadata-databuilder-1.0.jar";
    public static final String PACKAGE_PATH_BUILD_ALL = "file:/server/lib/kun-metadata-databuilder-schema.jar";
    public static final String CLASS_NAME = "com.miotech.kun.metadata.databuilder.schedule.DataBuilderOperator";

}
