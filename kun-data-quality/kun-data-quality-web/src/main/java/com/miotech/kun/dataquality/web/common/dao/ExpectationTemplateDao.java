package com.miotech.kun.dataquality.web.common.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ExpectationTemplateDao {

    private static final String TABLE_NAME = "kun_dq_expectation_template";
    private static final List<String> COLUMNS = ImmutableList.of("name", "granularity", "description", "converter", "display_parameters");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(ExpectationTemplate expectationTemplate) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(COLUMNS.toArray(new String[0]))
                .into(TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql,
                expectationTemplate.getName(),
                expectationTemplate.getGranularity(),
                expectationTemplate.getDescription(),
                expectationTemplate.getConverter(),
                expectationTemplate.getDisplayParameters());
    }

    public ExpectationTemplate fetchByName(String name) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("name = ?")
                .getSQL();

        try {
            return jdbcTemplate.queryForObject(sql, ExpectationMetricsExpressionRowMapper.INSTANCE, name);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return null;
        }
    }

    public List<ExpectationTemplate> fetchByGranularity(String granularity) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("granularity = ?")
                .getSQL();

        return jdbcTemplate.query(sql, ExpectationMetricsExpressionRowMapper.INSTANCE, granularity);
    }

    public static class ExpectationMetricsExpressionRowMapper implements RowMapper<ExpectationTemplate> {
        public static final ExpectationTemplateDao.ExpectationMetricsExpressionRowMapper INSTANCE = new ExpectationTemplateDao.ExpectationMetricsExpressionRowMapper();

        @Override
        public ExpectationTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ExpectationTemplate.newBuilder()
                    .withName(rs.getString("name"))
                    .withGranularity(rs.getString("granularity"))
                    .withDescription(rs.getString("description"))
                    .withConverter(rs.getString("converter"))
                    .withDisplayParameters(rs.getString("display_parameters"))
                    .build();
        }
    }

}
