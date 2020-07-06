package com.miotech.kun.workflow.common.operator.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.miotech.kun.workflow.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class OperatorDao {
    private static final Logger logger = LoggerFactory.getLogger(OperatorDao.class);

    private static final String DB_TABLE_NAME = "kun_wf_operator";

    private final DatabaseOperator dbOperator;

    @Inject
    public OperatorDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public Optional<Operator> fetchById(Long id) {
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");
        String sql = String.format("SELECT id, name, description, params, class_name, package FROM %s WHERE id = ?;", DB_TABLE_NAME);
        Operator operator = dbOperator.fetchOne(sql, OperatorMapper.INSTANCE, id);
        return Optional.ofNullable(operator);
    }

    public Optional<Operator> fetchByName(String name) {
        Preconditions.checkNotNull(name, "Invalid parameter `name`: found null object");
        String sql = String.format("SELECT id, name, description, params, class_name, package FROM %s WHERE name = ?;", DB_TABLE_NAME);
        Operator operator = dbOperator.fetchOne(sql, OperatorMapper.INSTANCE, name);
        return Optional.ofNullable(operator);
    }

    public List<Operator> fetchWithFilter(OperatorSearchFilter filters) {
        Preconditions.checkNotNull(filters, "Invalid parameter `filters`: found null object");
        Preconditions.checkArgument(Objects.nonNull(filters.getPageNum()) && filters.getPageNum() > 0, "Invalid page num: %d", filters.getPageNum());
        Preconditions.checkArgument(Objects.nonNull(filters.getPageSize()) && filters.getPageSize() > 0, "Invalid page size: %d", filters.getPageSize());
        boolean hasKeywordFilter = StringUtils.isNotBlank(filters.getKeyword());
        Integer offset = (filters.getPageNum() - 1) * filters.getPageSize();
        String baseSql = String.format("SELECT id, name, description, params, class_name, package FROM %s ", DB_TABLE_NAME);
        List<Operator> results;
        if (hasKeywordFilter) {
            String sql = baseSql + "WHERE name LIKE CONCAT('%', CAST(? AS TEXT), '%') LIMIT ? OFFSET ?";
            results = dbOperator.fetchAll(sql, OperatorMapper.INSTANCE, filters.getKeyword(), filters.getPageSize(), offset);
        } else {
            String sql = baseSql + "LIMIT ? OFFSET ?";
            results = dbOperator.fetchAll(sql, OperatorMapper.INSTANCE, filters.getPageSize(), offset);
        }
        return results;
    }

    public Integer fetchOperatorTotalCount() {
        return dbOperator.fetchOne("SELECT count(*) FROM " + DB_TABLE_NAME, rs -> rs.getInt(1));
    }

    public Integer fetchOperatorTotalCountWithFilter(OperatorSearchFilter filters) {
        Preconditions.checkNotNull(filters, "Invalid parameter `filters`: found null object");
        boolean hasKeywordFilter = StringUtils.isNotBlank(filters.getKeyword());
        String baseSql = String.format("SELECT count(*) FROM %s ", DB_TABLE_NAME);
        Integer count;
        if (hasKeywordFilter) {
            String sql = baseSql + "WHERE name LIKE CONCAT('%', CAST(? AS TEXT), '%')";
            count = dbOperator.fetchOne(sql, rs -> rs.getInt(1), filters.getKeyword());
        } else {
            count = dbOperator.fetchOne(baseSql, rs -> rs.getInt(1));
        }
        return count;
    }

    /**
     * Create an operator with id assigned
     * @param operator
     */
    public void create(Operator operator) {
        Preconditions.checkNotNull(operator, "Invalid parameter `operator`: found null object");
        Preconditions.checkNotNull(operator.getId(), "Invalid parameter `operator`: internal `id` not assigned");
        createWithId(operator, operator.getId());
    }

    /**
     * Create an operator with all of its properties but id is assigned individually
     * @param operator Operator object
     * @param id an ID to override the internal value of `operator` object
     */
    public void createWithId(Operator operator, Long id) {
        Preconditions.checkNotNull(operator, "Invalid parameter `operator`: found null object");
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");

        String sql = String.format("INSERT INTO %s " +
                "(id, name, description, params, class_name, package) " +
                "VALUES (?, ?, ?, ?, ?, ?);", DB_TABLE_NAME);
        dbOperator.update(
                sql,
                id,
                operator.getName(),
                operator.getDescription(),
                JSONUtils.toJsonString(operator.getParams()),
                operator.getClassName(),
                operator.getPackagePath()
        );
    }

    /**
     * Update an operator by given id with given props of an operator,
     * returns `true` if target row is affected.
     * returns `false` if target row not found.
     * @param id
     *          target operator id
     * @param operator
     *          an operator object as properties provider (except `id`)
     * @return if target row is affected
     */
    public boolean updateById(Long id, Operator operator) {
        // 1. Validate arguments
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");
        Preconditions.checkNotNull(operator, "Invalid parameter `operator`: found null object");

        String sql = String.format("UPDATE %s SET " +
                "name = ?, description = ?, params = ?, class_name = ?, package = ? WHERE id = ?;", DB_TABLE_NAME);
        int affectedRows = dbOperator.update(
                sql,
                operator.getName(),
                operator.getDescription(),
                JSONUtils.toJsonString(operator.getParams()),
                operator.getClassName(),
                operator.getPackagePath(),
                id
        );
        return affectedRows > 0;
    }

    /**
     * Delete an operator by given id with given props of an operator,
     * returns `true` if target row is affected.
     * returns `false` if target row not found.
     * @param id target operator id
     * @return if target row is affected
     */
    public boolean deleteById(Long id) {
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");
        String sql = String.format("DELETE FROM %s WHERE id = ?;", DB_TABLE_NAME);
        int affectedRows = dbOperator.update(sql, id);
        return affectedRows > 0;
    }

    private static class OperatorMapper implements ResultSetMapper<Operator> {
        public static final OperatorMapper INSTANCE = new OperatorMapper();

        @Override
        public Operator map(ResultSet rs) throws SQLException {
            return Operator.newBuilder()
                    .withId(rs.getLong("id"))
                    .withName(rs.getString("name"))
                    .withParams(JSONUtils.jsonToObject(rs.getString("params"), new TypeReference<List<Param>>() {}))
                    .withDescription(rs.getString("description"))
                    .withClassName(rs.getString("class_name"))
                    .withPackagePath(rs.getString("package"))
                    .build();
        }
    }
}
