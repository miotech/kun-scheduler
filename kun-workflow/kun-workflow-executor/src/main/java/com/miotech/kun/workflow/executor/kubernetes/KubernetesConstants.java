package com.miotech.kun.workflow.executor.kubernetes;

//管理kubernetes pod的常量，如lables等
public class KubernetesConstants {
    public static final String KUN_TASK_ATTEMPT_ID = "kuntaskattemptid";
    public static final String KUN_WORKFLOW = "kunworkflow";
    public static final String CPU_DEFAULT = "1";
    public static final String MEMORY_DEFAULT = "512";
    public static final String CPU_REQUEST = "0.5";
    public static final String MEMORY_REQUEST = "256";
    public static final String IMAGE_PULL_POLICY = "Always";
    public static final String POD_IMAGE_NAME = "kunoperator";
    public static final String TASK_QUEUE = "task_queue";

}
