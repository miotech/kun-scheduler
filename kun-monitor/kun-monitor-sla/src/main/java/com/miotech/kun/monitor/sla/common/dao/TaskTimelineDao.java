package com.miotech.kun.monitor.sla.common.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.monitor.sla.model.BacktrackingTaskDefinition;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class TaskTimelineDao {

    private static final String TIMELINE_TABLE_NAME = "kun_dp_task_timeline";
    private static final String TIMELINE_MODEL_NAME = "timeline";
    private static final List<String> TIMELINE_COLS = ImmutableList.of("id", "task_run_id", "definition_id", "level", "deadline",
            "root_definition_id", "created_at", "updated_at");
    private static final List<String> TIMELINE_INSERT_COLS = ImmutableList.of("task_run_id", "definition_id", "level", "deadline",
            "root_definition_id", "created_at", "updated_at");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(TaskTimeline taskTimeline) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(TIMELINE_INSERT_COLS.toArray(new String[0]))
                .into(TIMELINE_TABLE_NAME)
                .asPrepared()
                .getSQL();

        jdbcTemplate.update(
                sql,
                taskTimeline.getTaskRunId(),
                taskTimeline.getDefinitionId(),
                taskTimeline.getLevel(),
                taskTimeline.getDeadline(),
                taskTimeline.getRootDefinitionId(),
                taskTimeline.getCreatedAt(),
                taskTimeline.getUpdatedAt()
        );
    }

    public List<TaskTimeline> fetchByDefinitionId(Long definitionId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TIMELINE_COLS.toArray(new String[0]))
                .from(TIMELINE_TABLE_NAME)
                .where("definition_id = ?")
                .getSQL();
        return jdbcTemplate.query(sql, TaskTimelineMapper.INSTANCE, definitionId);
    }

    public List<TaskTimeline> fetchByDeadline(String deadline) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TIMELINE_COLS.toArray(new String[0]))
                .from(TIMELINE_TABLE_NAME)
                .where("deadline = ?")
                .getSQL();
        return jdbcTemplate.query(sql, TaskTimelineMapper.INSTANCE, deadline);
    }

    public BacktrackingTaskDefinition fetchBacktrackingByDefinitionId(Long taskDefinitionId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("level", "root_definition_id", "deadline")
                .from(TIMELINE_TABLE_NAME)
                .where("definition_id = ? and root_definition_id is not null")
                .orderBy("id desc")
                .limit(1)
                .getSQL();

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                            BacktrackingTaskDefinition.builder()
                                    .definitionId(rs.getLong("root_definition_id"))
                                    .priority(rs.getInt("level"))
                                    .deadline(LocalDateTime.parse(rs.getString("deadline"), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).atOffset(ZoneOffset.UTC))
                                    .build()
                    , taskDefinitionId);
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }

    public static class TaskTimelineMapper implements RowMapper<TaskTimeline> {
        public static final TaskTimelineMapper INSTANCE = new TaskTimelineMapper();

        @Override
        public TaskTimeline mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long rootDefinitionId = rs.getLong("root_definition_id");
            if (rs.wasNull()) {
                rootDefinitionId = null;
            }
            return TaskTimeline.newBuilder()
                    .withId(rs.getLong("id"))
                    .withTaskRunId(rs.getLong("task_run_id"))
                    .withDefinitionId(rs.getLong("definition_id"))
                    .withLevel(rs.getInt("level"))
                    .withDeadline(rs.getString("deadline"))
                    .withRootDefinitionId(rootDefinitionId)
                    .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                    .withUpdatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("updated_at")))
                    .build();
        }
    }

}
