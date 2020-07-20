package com.miotech.kun.commons.query;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: Jie Chen
 * @created: 2020/7/9
 */
public class QuerySite {

    private String siteId;

    private Long datasetId;

    private Long datasourceId;

    private String urlPostfix;

    private QuerySite() {}

    public String getSiteId() {
        return siteId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

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

        private String siteId;

        private Long datasetId;

        private Long datasourceId;

        private String urlPostfix;

        private Builder() {
        }

        public Builder datasetId(Long datasetId) {
            if (StringUtils.isEmpty(siteId)) {
                siteId = String.valueOf(datasetId);
            }
            this.datasetId = datasetId;
            return this;
        }

        public Builder datasourceId(Long datasourceId) {
            if (StringUtils.isEmpty(siteId)) {
                siteId = String.valueOf(datasourceId);
            }
            this.datasourceId = datasourceId;
            return this;
        }

        public Builder urlPostfix(String urlPostfix) {
            this.urlPostfix = urlPostfix;
            return this;
        }

        public QuerySite build() {
            QuerySite querySite = new QuerySite();
            querySite.siteId = siteId;
            querySite.datasetId = this.datasetId;
            querySite.datasourceId = this.datasourceId;
            querySite.urlPostfix = this.urlPostfix;
            return querySite;
        }
    }
}
