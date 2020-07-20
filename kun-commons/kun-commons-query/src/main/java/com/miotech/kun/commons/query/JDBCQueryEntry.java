package com.miotech.kun.commons.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/9
 */
public class JDBCQueryEntry implements QueryEntry {

    private String queryString;

    private List<Object> queryArgs;

    private JDBCQueryEntry() {}

    public String getQueryString() {
        return queryString;
    }

    List<Object> getQueryArgs() {
        return queryArgs;
    }

    Object[] getQueryArgsArray() {
        if (queryArgs != null) {
            return queryArgs.toArray();
        }
        return null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String queryString;
        private List<Object> queryArgs = new ArrayList<>();

        private Builder() {
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder queryArgs(Object... args) {
            this.queryArgs.addAll(Arrays.asList(args));
            return this;
        }

        public Builder queryArgs(List<Object> args) {
            this.queryArgs = args;
            return this;
        }

        public JDBCQueryEntry build() {
            JDBCQueryEntry jdbcQueryEntry = new JDBCQueryEntry();
            jdbcQueryEntry.queryString = this.queryString;
            jdbcQueryEntry.queryArgs = this.queryArgs;
            return jdbcQueryEntry;
        }
    }
}
