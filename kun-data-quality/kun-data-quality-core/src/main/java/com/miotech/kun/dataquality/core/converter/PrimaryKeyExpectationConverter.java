package com.miotech.kun.dataquality.core.converter;

import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.assertion.EqualsAssertion;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class PrimaryKeyExpectationConverter implements ExpectationConverter {

    private final static String BASE_SQL = "select sum(case when t.c > 1 then 1 else 0 end) %s from (select count(*) c from %s group by %s) t";
    private final static String FIELD = "c";


    @Override
    public Metrics convertMetrics(Map<String, Object> payload) {
        List<String> fields = (List<String>) payload.get("fields");
        String databaseName = (String) payload.get("databaseName");
        String tableName = (String) payload.get("tableName");

        String sql = generateSQL(databaseName + "." + tableName, fields);
        return SQLMetrics.newBuilder()
                .withSql(sql)
                .withField(FIELD)
                .build();
    }

    @Override
    public Assertion convertAssertion(Map<String, Object> payload) {
        return new EqualsAssertion("0");
    }

    private String generateSQL(String table, List<String> fields) {
        return String.format(BASE_SQL, FIELD, table, StringUtils.join(fields, ","));
    }

}
