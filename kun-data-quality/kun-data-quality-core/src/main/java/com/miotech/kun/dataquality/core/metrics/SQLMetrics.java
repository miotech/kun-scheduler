package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.metadata.common.connector.Connector;
import com.miotech.kun.metadata.common.connector.ConnectorFactory;
import com.miotech.kun.metadata.common.connector.Query;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;

public class SQLMetrics extends Metrics {

    private final String sql;

    private final String field;

    @JsonCreator
    public SQLMetrics(@JsonProperty("name") String name,
                      @JsonProperty("description") String description,
                      @JsonProperty("granularity") Granularity granularity,
                      @JsonProperty("dataset") Dataset dataset,
                      @JsonProperty("sql") String sql,
                      @JsonProperty("field") String field) {
        super(MetricsType.SQL, name, description, granularity, dataset);
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
    public MetricsCollectedResult collect() {
        DataSource dataSource = super.getDataset().getDataSource();
        Connector connector = null;
        try {
            connector = ConnectorFactory.generateConnector(dataSource);
            Query query = new Query(null, null, removeEndSemicolon(this.sql));
            ResultSet rs = connector.query(query);

            if (rs.next()) {
                return new SQLMetricsCollectedResult(this, DateTimeUtils.now(), rs.getObject(this.field).toString());
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
                .withName(this.getName())
                .withDescription(this.getDescription())
                .withGranularity(this.getGranularity())
                .withDataset(this.getDataset())
                .withSql(this.sql)
                .withField(this.field)
                ;
    }

    public static final class Builder {
        private String sql;
        private String field;
        private String name;
        private String description;
        private Granularity granularity;
        private Dataset dataset;

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

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withGranularity(Granularity granularity) {
            this.granularity = granularity;
            return this;
        }

        public Builder withDataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public SQLMetrics build() {
            return new SQLMetrics(name, description, granularity, dataset, sql, field);
        }
    }
}
