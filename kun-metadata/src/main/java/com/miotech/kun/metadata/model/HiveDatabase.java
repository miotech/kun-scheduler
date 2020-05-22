package com.miotech.kun.metadata.model;

import com.miotech.kun.metadata.constant.DatabaseType;

public class HiveDatabase extends Database {

    public HiveDatabase() {
        super(DatabaseType.HIVE);
    }

    private String metaStoreUrl;

    private String metaStoreUsername;

    private String metaStorePassword;

    public String getMetaStoreUrl() {
        return metaStoreUrl;
    }

    public void setMetaStoreUrl(String metaStoreUrl) {
        this.metaStoreUrl = metaStoreUrl;
    }

    public String getMetaStoreUsername() {
        return metaStoreUsername;
    }

    public void setMetaStoreUsername(String metaStoreUsername) {
        this.metaStoreUsername = metaStoreUsername;
    }

    public String getMetaStorePassword() {
        return metaStorePassword;
    }

    public void setMetaStorePassword(String metaStorePassword) {
        this.metaStorePassword = metaStorePassword;
    }

    public HiveDatabase(String metaStoreUrl, String metaStoreUsername, String metaStorePassword) {
        this.metaStoreUrl = metaStoreUrl;
        this.metaStoreUsername = metaStoreUsername;
        this.metaStorePassword = metaStorePassword;
    }

}
