package com.miotech.kun.dataplatform.common.utils;

import com.miotech.kun.commons.utils.IdGenerator;

public class DataPlatformIdGenerator {
    private DataPlatformIdGenerator() {
    }

    /**
     * Generate a snowflake ID for dataplatform TaskDefinition
     * @return Snowflake ID
     */
    public static long nextTaskDefinitionId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform TaskTry
     * @return Snowflake ID
     */
    public static long nextTaskTryId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform definitionId
     * @return Snowflake ID
     */
    public static long nextDefinitionId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform commitId
     * @return Snowflake ID
     */
    public static long nextCommitId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform deployId
     * @return Snowflake ID
     */
    public static long nextDeployId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform deployId
     * @return Snowflake ID
     */
    public static long nextDeployedTaskId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform datastoreId
     * @return Snowflake ID
     */
    public static long nextDatastoreId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for dataplatform datasetId
     * @return Snowflake ID
     */
    public static long nextDatasetId() {
        return IdGenerator.getInstance().nextId();
    }

}
