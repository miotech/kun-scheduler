package com.miotech.kun.commons.query;

/**
 * @author: Jie Chen
 * @created: 2020/7/9
 */
public class JDBCQueryBuilder {

    private QuerySite querySite;
    private JDBCQueryEntry queryParams;

    private JDBCQueryBuilder() {
    }

    public static JDBCQueryBuilder builder() {
        return new JDBCQueryBuilder();
    }

    public JDBCQueryBuilder querySite(QuerySite querySite) {
        this.querySite = querySite;
        return this;
    }

    public JDBCQueryBuilder queryParams(JDBCQueryEntry queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public JDBCQuery build() {
        JDBCQuery JDBCQuery = new JDBCQuery();
        JDBCQuery.setQuerySite(this.querySite);
        JDBCQuery.setQueryEntry(this.queryParams);
        return JDBCQuery;
    }
}
