package com.miotech.kun.commons.query;

/**
 * @author: Jie Chen
 * @created: 2020/7/9
 */
public class QuerySite {

    private Long datasourceId;

    private String databaseName;

    public Long getDatasourceId() {
        return datasourceId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long datasourceId;
        private String databaseName;

        private Builder() {
        }

        public Builder datasourceId(Long datasourceId) {
            this.datasourceId = datasourceId;
            return this;
        }

        public Builder databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public QuerySite build() {
            QuerySite querySite = new QuerySite();
            querySite.datasourceId = this.datasourceId;
            querySite.databaseName = this.databaseName;
            return querySite;
        }
    }
}
