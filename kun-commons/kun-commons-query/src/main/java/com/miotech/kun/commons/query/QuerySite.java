package com.miotech.kun.commons.query;

/**
 * @author: Jie Chen
 * @created: 2020/7/9
 */
public class QuerySite {

    private Long datasourceId;

    private String urlPostfix;

    public Long getDatasourceId() {
        return datasourceId;
    }

    public String getUrlPostfix() {
        return urlPostfix;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long datasourceId;

        private String urlPostfix;

        private Builder() {
        }

        public Builder datasourceId(Long datasourceId) {
            this.datasourceId = datasourceId;
            return this;
        }

        public Builder urlPostfix(String urlPostfix) {
            this.urlPostfix = urlPostfix;
            return this;
        }

        public QuerySite build() {
            QuerySite querySite = new QuerySite();
            querySite.datasourceId = this.datasourceId;
            querySite.urlPostfix = this.urlPostfix;
            return querySite;
        }
    }
}
