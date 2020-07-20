package com.miotech.kun.commons.query;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/8
 */
public class JDBCQuery implements Query<JDBCQueryEntry> {

    private QuerySite querySite;

    private JDBCQueryEntry queryEntry;

    private JDBCQuery() {}

    @Override
    public QuerySite getQuerySite() {
        return querySite;
    }

    @Override
    public JDBCQueryEntry getQueryEntry() {
        return queryEntry;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private QuerySite.Builder querySite;
        private JDBCQueryEntry.Builder queryEntry;

        private Builder() {
            this.querySite = QuerySite.newBuilder();
            this.queryEntry = JDBCQueryEntry.newBuilder();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder datasetId(Long datasetId) {
            this.querySite.datasetId(datasetId);
            return this;
        }

        public Builder datasourceId(Long datasourceId) {
            this.querySite.datasourceId(datasourceId);
            return this;
        }

        public Builder urlPostfix(String urlPostfix) {
            this.querySite.urlPostfix(urlPostfix);
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryEntry.queryString(queryString);
            return this;
        }

        public Builder queryArgs(Object... args) {
            this.queryEntry.queryArgs(args);
            return this;
        }

        public Builder queryArgs(List<Object> args) {
            this.queryEntry.queryArgs(args);
            return this;
        }

        public JDBCQuery build() {
            JDBCQuery jDBCQuery = new JDBCQuery();
            jDBCQuery.querySite = querySite.build();
            jDBCQuery.queryEntry = queryEntry.build();
            return jDBCQuery;
        }
    }
}
