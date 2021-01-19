package com.miotech.kun.dataplatform.common.backfill.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class BackfillDao {
    private static final String BACKFILL_TABLE_NAME = "kun_dp_backfill";

    private static final String BACKFILL_MODEL_NAME = "backfill";

    private static final String BACKFILL_TASK_RUN_RELATION_TABLE_NAME = "kun_dp_backfill_task_run_relation";

    private static final List<String> BACKFILL_TABLE_COLS = ImmutableList.of("id", "name", "creator", "create_time", "update_time");

    private static final List<String> BACKFILL_TASK_RUN_RELATION_TABLE_COLS =  ImmutableList.of("backfill_id", "task_run_id", "task_id", "task_definition_id");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BackfillMapper backfillMapper = null;

    private BackfillMapper getBackfillMapperInstance() {
        if (this.backfillMapper == null) {
            this.backfillMapper = new BackfillMapper(jdbcTemplate);
        }
        return this.backfillMapper;
    }

    public String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(BACKFILL_MODEL_NAME, BACKFILL_TABLE_COLS);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(BACKFILL_TABLE_NAME, BACKFILL_MODEL_NAME)
                .autoAliasColumns();
        if (Strings.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<Backfill> fetchById(Long id) {
        return jdbcTemplate.query(
                getSelectSQL(BACKFILL_MODEL_NAME + ".id = ?"),
                getBackfillMapperInstance(),
                id
        ).stream().findAny();
    }

    public static class BackfillMapper implements RowMapper<Backfill> {
        private final JdbcTemplate jdbcTemplate;

        public BackfillMapper(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @SuppressWarnings("SqlResolve")
        @Override
        public Backfill mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long backfillId = rs.getLong(BACKFILL_MODEL_NAME + "_id");
            Backfill.BackFillBuilder builder = Backfill.newBuilder()
                    .withId(backfillId)
                    .withName(rs.getString(BACKFILL_MODEL_NAME + "_name"))
                    .withCreator(rs.getLong(BACKFILL_MODEL_NAME + "_creator"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(BACKFILL_MODEL_NAME + "_create_time")))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(BACKFILL_MODEL_NAME + "_update_time")));

            // search related task run ids and construct array list
            List<Long> taskRunIds = new LinkedList<>();
            List<Long> taskDefinitionIds = new LinkedList<>();
            jdbcTemplate.query(
                    "SELECT backfill_id, task_run_id, task_id, task_definition_id FROM " + BACKFILL_TASK_RUN_RELATION_TABLE_NAME + " WHERE backfill_id = ?",
                    rsRelation -> {
                        taskRunIds.add(rsRelation.getLong("task_run_id"));
                        taskDefinitionIds.add(rsRelation.getLong("task_definition_id"));
                    },
                    backfillId
            );
            builder.withTaskRunIds(taskRunIds)
                    .withTaskDefinitionIds(taskDefinitionIds);
            return builder.build();
        }
    }
}
