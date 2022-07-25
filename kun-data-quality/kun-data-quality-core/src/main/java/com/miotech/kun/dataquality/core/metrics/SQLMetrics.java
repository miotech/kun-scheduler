package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.connector.Connector;
import com.miotech.kun.metadata.common.connector.ConnectorFactory;
import com.miotech.kun.metadata.common.connector.Query;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class SQLMetrics extends Metrics<String> {

    private static final Logger logger = LoggerFactory.getLogger(SQLMetrics.class);

    private final String sql;

    private final String field;

    @JsonCreator
    public SQLMetrics(@JsonProperty("sql") String sql,
                      @JsonProperty("field") String field) {
        super(MetricsType.SQL);
        this.sql = sql;
        this.field = field;
    }

    public String getSql() {
        return sql;
    }

    public String getField() {
        return field;
    }

    @Override
    public MetricsCollectedResult<String> collect(CollectContext context) {
        DataSource dataSource = context.getDataSource();
        Connector connector = null;
        try {
            connector = ConnectorFactory.generateConnector(dataSource);
            Query query = new Query(null, null, removeEndSemicolon(this.sql));
            logger.debug("collect query: {}", JSONUtils.toJsonString(query));
            ResultSet rs = connector.query(query);

            if (rs.next()) {
                return new MetricsCollectedResult(this, DateTimeUtils.now(), rs.getObject(this.field).toString());
            } else {
                throw new IllegalStateException("No data returned");
            }
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            if (connector != null) {
                connector.close();
            }
        }
    }

    public String removeEndSemicolon(String sql) {
        String trimSQL = sql.trim();
        return StringUtils.removeEnd(trimSQL, ";").trim();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withSql(this.sql)
                .withField(this.field)
                ;
    }

    public static final class Builder {
        private String sql;
        private String field;

        private Builder() {
        }

        public Builder withSql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        public SQLMetrics build() {
            return new SQLMetrics(sql, field);
        }
    }
}
