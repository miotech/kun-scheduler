package com.miotech.kun.workflow.core.model.worker;

public class DatabaseConfig {

    private String datasourceUrl;
    private String datasourceUser;
    private String datasourcePassword;
    private String datasourceDriver;
    private Integer datasourceMaxPoolSize;
    private Integer datasourceMinIdle;
    private String neo4juri;
    private String neo4jUser;
    private String neo4jPassword;

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public void setDatasourceUrl(String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    public String getDatasourceUser() {
        return datasourceUser;
    }

    public void setDatasourceUser(String datasourceUser) {
        this.datasourceUser = datasourceUser;
    }

    public String getDatasourcePassword() {
        return datasourcePassword;
    }

    public void setDatasourcePassword(String datasourcePassword) {
        this.datasourcePassword = datasourcePassword;
    }

    public String getDatasourceDriver() {
        return datasourceDriver;
    }

    public void setDatasourceDriver(String datasourceDriver) {
        this.datasourceDriver = datasourceDriver;
    }

    public Integer getDatasourceMaxPoolSize() {
        return datasourceMaxPoolSize;
    }

    public void setDatasourceMaxPoolSize(Integer datasourceMaxPoolSize) {
        this.datasourceMaxPoolSize = datasourceMaxPoolSize;
    }

    public Integer getDatasourceMinIdle() {
        return datasourceMinIdle;
    }

    public void setDatasourceMinIdle(Integer datasourceMinIdle) {
        this.datasourceMinIdle = datasourceMinIdle;
    }

    public String getNeo4juri() {
        return neo4juri;
    }

    public void setNeo4juri(String neo4juri) {
        this.neo4juri = neo4juri;
    }

    public String getNeo4jUser() {
        return neo4jUser;
    }

    public void setNeo4jUser(String neo4jUser) {
        this.neo4jUser = neo4jUser;
    }

    public String getNeo4jPassword() {
        return neo4jPassword;
    }

    public void setNeo4jPassword(String neo4jPassword) {
        this.neo4jPassword = neo4jPassword;
    }
}
