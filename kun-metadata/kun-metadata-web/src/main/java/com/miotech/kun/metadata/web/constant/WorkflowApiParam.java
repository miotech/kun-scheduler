package com.miotech.kun.metadata.web.constant;

public class WorkflowApiParam {

    private WorkflowApiParam() {
    }

    public static final String OPERATOR_NAME = "Metadata DataBuilder Operator";
    public static final String TASK_NAME = "Metadata DataBuilder Task";
    public static final String OPERATORS = "operators";
    public static final String TASKS = "tasks";
    public static final String TASKS_RUN = "tasks/_run";
    public static final String PACKAGE_PATH = "file:/server/lib/kun-metadata-databuilder-1.0.jar";
    public static final String CLASS_NAME = "com.miotech.kun.metadata.databuilder.schedule.DataBuilderOperator";

}
