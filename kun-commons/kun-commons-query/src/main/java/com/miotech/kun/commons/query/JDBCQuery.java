package com.miotech.kun.commons.query;

/**
 * @author: Jie Chen
 * @created: 2020/7/8
 */
public class JDBCQuery implements Query<JDBCQueryEntry> {

    private QuerySite querySite;

    private JDBCQueryEntry queryEntry;

    public void setQuerySite(QuerySite querySite) {
        this.querySite = querySite;
    }

    public void setQueryEntry(JDBCQueryEntry queryEntry) {
        this.queryEntry = queryEntry;
    }

    @Override
    public QuerySite getQuerySite() {
        return querySite;
    }

    @Override
    public JDBCQueryEntry getQueryEntry() {
        return queryEntry;
    }
}
