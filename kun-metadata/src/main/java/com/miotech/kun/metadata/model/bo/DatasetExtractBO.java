package com.miotech.kun.metadata.model.bo;

public class DatasetExtractBO extends BaseExtractBO {

    private Long databaseId;

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    public static boolean checkParam(DatasetExtractBO extractBO) {
        return false;
    }

}
