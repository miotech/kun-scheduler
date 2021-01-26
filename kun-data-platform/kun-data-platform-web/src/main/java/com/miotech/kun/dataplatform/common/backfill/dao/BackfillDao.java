package com.miotech.kun.dataplatform.common.backfill.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.db.sql.WhereClause;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BackfillDao {
    private static final Logger logger = LoggerFactory.getLogger(BackfillDao.class);

    private static final String BACKFILL_TABLE_NAME = "kun_dp_backfill";

    private static final String BACKFILL_MODEL_NAME = "backfill";

    private static final String BACKFILL_TASK_RUN_RELATION_TABLE_NAME = "kun_dp_backfill_task_run_relation";

    private static final List<String> BACKFILL_TABLE_COLS = ImmutableList.of("id", "name", "creator", "create_time", "update_time");

    private static final List<String> BACKFILL_TASK_RUN_RELATION_TABLE_COLS =  ImmutableList.of("backfill_id", "task_run_id", "task_id", "task_definition_id");

    private static final String INSERT_BACKFILL_TABLE_SQL_STMT =
            "INSERT INTO " + BACKFILL_TABLE_NAME + " (" + BACKFILL_TABLE_COLS.stream().collect(Collectors.joining(","))
            + ") VALUES (" + StringUtils.repeatJoin("?", ",", BACKFILL_TABLE_COLS.size()) + ")";

    private static final String INSERT_BACKFILL_RELATION_SQL_STMT =
            "INSERT INTO " + BACKFILL_TASK_RUN_RELATION_TABLE_NAME + "(" + BACKFILL_TASK_RUN_RELATION_TABLE_COLS.stream().collect(Collectors.joining(","))
            + ") VALUES (" + StringUtils.repeatJoin("?", ",", BACKFILL_TASK_RUN_RELATION_TABLE_COLS.size()) + ")";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BackfillMapper backfillMapper = null;

    private BackfillMapper getBackfillMapperInstance() {
        if (this.backfillMapper == null) {
            this.backfillMapper = new BackfillMapper(jdbcTemplate);
        }
        return this.backfillMapper;
    }

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(BACKFILL_MODEL_NAME, BACKFILL_TABLE_COLS);
        SQLBuilder builder = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(BACKFILL_TABLE_NAME, BACKFILL_MODEL_NAME)
                .autoAliasColumns();
        if (Strings.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    private String getSelectSQL(String whereClause, Integer limit, Integer offset, String orderBy) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(BACKFILL_MODEL_NAME, BACKFILL_TABLE_COLS);
        SQLBuilder builder = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(BACKFILL_TABLE_NAME, BACKFILL_MODEL_NAME)
                .limit(limit)
                .offset(offset)
                .orderBy(orderBy)
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

    public PageResult<Backfill> search(BackfillSearchParams searchParams) {
        // Preconditions check
        Preconditions.checkNotNull(searchParams);
        Preconditions.checkArgument(Objects.nonNull(searchParams.getPageNumber()) && searchParams.getPageNumber() > 0, "page number should be positive");
        Preconditions.checkArgument(Objects.nonNull(searchParams.getPageSize()) && searchParams.getPageSize() > 0, "page size should be positive");
        Preconditions.checkArgument(
                Objects.isNull(searchParams.getSortKey()) || BACKFILL_TABLE_COLS.contains(searchParams.getSortKey()),
                String.format("Invalid sort key: %s", searchParams.getSortKey())
        );

        // Load where, sort and order parameters
        int limit = Objects.nonNull(searchParams.getPageSize()) ? searchParams.getPageSize() : 100;
        int offset = Objects.nonNull(searchParams.getPageNumber()) ? limit * (searchParams.getPageNumber() - 1) : 0;
        String sortKey = Objects.isNull(searchParams.getSortKey()) ? "id" : searchParams.getSortKey();
        String sortOrder = Objects.isNull(searchParams.getSortOrder()) ? "DESC" : searchParams.getSortOrder().getSqlString();
        WhereClause whereClause = getWhereClause(searchParams);

        // Generate SQL
        String sql = getSelectSQL(whereClause.getSqlSegment(), limit, offset, String.format("%s %s", sortKey, sortOrder));

        // do query
        List<Backfill> result = jdbcTemplate.query(
                sql,
                getBackfillMapperInstance(),
                whereClause.getParams()
        );
        return new PageResult<>(searchParams.getPageSize(), searchParams.getPageNumber(), countResults(searchParams), result);
    }

    public int countResults(BackfillSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams);
        WhereClause whereClause = getWhereClause(searchParams);
        String sql = "SELECT COUNT(*) AS count FROM " + BACKFILL_TABLE_NAME + " AS " + BACKFILL_MODEL_NAME
                + " WHERE " + whereClause.getSqlSegment();

        //noinspection ConstantConditions
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("count"),
                whereClause.getParams()
        ).get(0);
    }

    private WhereClause getWhereClause(BackfillSearchParams searchParams) {
        StringBuilder whereClauseBuilder = new StringBuilder("(1 = 1) ");
        List<Object> sqlParams = new LinkedList<>();

        if (Objects.nonNull(searchParams.getCreators())) {
            whereClauseBuilder.append(
                    String.format(" AND (" + BACKFILL_MODEL_NAME + ".creator IN (%s))", StringUtils.repeatJoin("?", ",", searchParams.getCreators().size()))
            );
            sqlParams.addAll(searchParams.getCreators());
        }
        if (Strings.isNotBlank(searchParams.getName())) {
            whereClauseBuilder.append(" AND (" + BACKFILL_MODEL_NAME + ".name ILIKE CONCAT('%', CAST(? AS TEXT), '%'))");
            sqlParams.add(searchParams.getName().trim());
        }
        if (Objects.nonNull(searchParams.getTimeRngStart())) {
            whereClauseBuilder.append(" AND (" + BACKFILL_MODEL_NAME + ".create_time >= ?)");
            sqlParams.add(searchParams.getTimeRngStart());
        }
        if (Objects.nonNull(searchParams.getTimeRngEnd())) {
            whereClauseBuilder.append(" AND (" + BACKFILL_MODEL_NAME + ".create_time <= ?)");
            sqlParams.add(searchParams.getTimeRngEnd());
        }

        logger.debug("Searching backfills with SQL statement: {}", whereClauseBuilder.toString());
        return new WhereClause(whereClauseBuilder.toString(), sqlParams.toArray());
    }

    /**
     * Create a backfill instance
     * @param backfill backfill instance
     * @return persisted instance
     * @throws DuplicateKeyException if primary key conflict observed
     */
    @Transactional
    public Backfill create(Backfill backfill) {
        checkBackfillArgumentBeforeCreate(backfill);

        // insert record
        jdbcTemplate.update(INSERT_BACKFILL_TABLE_SQL_STMT, backfill.getId(), backfill.getName(), backfill.getCreator(), backfill.getCreateTime(), backfill.getUpdateTime());

        // insert relations
        List<Object[]> relationInsertParams = new ArrayList<>();
        for (int i = 0; i < backfill.getTaskRunIds().size(); ++i) {
            Object[] insertParams = { backfill.getId(), backfill.getTaskRunIds().get(i), backfill.getWorkflowTaskIds().get(i), backfill.getTaskDefinitionIds().get(i) };
            relationInsertParams.add(insertParams);
        }
        jdbcTemplate.batchUpdate(INSERT_BACKFILL_RELATION_SQL_STMT, relationInsertParams);

        // return persisted instance
        Optional<Backfill> persistedBackfill = fetchById(backfill.getId());
        if (!persistedBackfill.isPresent()) {
            throw new IllegalStateException(String.format("Cannot retrieve persisted backfill with id: %s", backfill.getId()));
        }
        // else
        return persistedBackfill.get();
    }

    private static void checkBackfillArgumentBeforeCreate(Backfill backfill) {
        Preconditions.checkNotNull(backfill);
        Preconditions.checkNotNull(backfill.getTaskRunIds());
        Preconditions.checkNotNull(backfill.getWorkflowTaskIds());
        Preconditions.checkNotNull(backfill.getTaskDefinitionIds());
        Preconditions.checkArgument(
                backfill.getTaskDefinitionIds().size() == backfill.getTaskRunIds().size(),
                "Task runs should have same size as task definitions."
        );
        Preconditions.checkArgument(
                backfill.getWorkflowTaskIds().size() == backfill.getTaskRunIds().size(),
                "Task runs should have same size as workflow task ids."
        );
        Preconditions.checkArgument(backfill.getTaskRunIds().stream().noneMatch(Objects::isNull), "Cannot have null id in task run ids");
        Preconditions.checkArgument(backfill.getTaskDefinitionIds().stream().noneMatch(Objects::isNull), "Cannot have null id in task definitions");
        Preconditions.checkArgument(backfill.getWorkflowTaskIds().stream().noneMatch(Objects::isNull), "Cannot have null id in workflow task ids");
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
            List<Long> workflowTaskIds = new LinkedList<>();
            jdbcTemplate.query(
                    "SELECT backfill_id, task_run_id, task_id, task_definition_id FROM " + BACKFILL_TASK_RUN_RELATION_TABLE_NAME + " WHERE backfill_id = ?",
                    rsRelation -> {
                        taskRunIds.add(rsRelation.getLong("task_run_id"));
                        workflowTaskIds.add(rsRelation.getLong("task_id"));
                        taskDefinitionIds.add(rsRelation.getLong("task_definition_id"));
                    },
                    backfillId
            );
            builder.withTaskRunIds(taskRunIds)
                    .withWorkflowTaskIds(workflowTaskIds)
                    .withTaskDefinitionIds(taskDefinitionIds);
            return builder.build();
        }
    }
}
