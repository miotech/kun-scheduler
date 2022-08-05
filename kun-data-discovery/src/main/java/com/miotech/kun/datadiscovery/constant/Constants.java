package com.miotech.kun.datadiscovery.constant;

import com.miotech.kun.workflow.core.model.common.Tag;

public class Constants {
    public static final String TAG_TASK_TYPE_NAME = "type";
    public static final String DATA_PLATFORM_TAG_VALUE_MANUAL_RUN = "manual_run";
    public static final Tag TAG_TYPE_MANUAL_RUN = new Tag(TAG_TASK_TYPE_NAME, DATA_PLATFORM_TAG_VALUE_MANUAL_RUN);

    public static final String INCONSISTENT_INFO = "The  rows is inconsistent with the data";
    public static final String CONSTRAINT_INFO = "Data does not meet constraints";
    public static final String DATA_FORMAT_ERROR = "data in wrong format";
    public static final Integer INPUT_MAX_LINE = 10000;
}
