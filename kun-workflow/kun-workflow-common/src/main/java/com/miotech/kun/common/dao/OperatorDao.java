package com.miotech.kun.common.dao;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.filters.OperatorSearchFilter;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
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

import static com.miotech.kun.common.helpers.OperatorDaoHelpers.jsonStringToParams;
import static com.miotech.kun.common.helpers.OperatorDaoHelpers.paramsToJsonString;

@Singleton
public class OperatorDao {
    private static final Logger logger = LoggerFactory.getLogger(OperatorDao.class);

    private static final String DB_TABLE_NAME = "kun_wf_operator";

    private final DatabaseOperator dbOperator;

    @Inject
    public OperatorDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public Optional<Operator> getById(Long id) {
        Preconditions.checkArgument(Objects.nonNull(id), "Invalid parameter `id`: found null object");
        String sql = String.format("SELECT id, name, description, params, class_name, package FROM %s WHERE id = ?;", DB_TABLE_NAME);
        Operator operator = dbOperator.fetchOne(sql, OperatorMapper.INSTANCE, id);
        return Optional.ofNullable(operator);
    }

    public Optional<Operator> getByName(String name) {
        Preconditions.checkArgument(Objects.nonNull(name), "Invalid parameter `name`: found null object");
        String sql = String.format("SELECT id, name, description, params, class_name, package FROM %s WHERE name = ?;", DB_TABLE_NAME);
        Operator operator = dbOperator.fetchOne(sql, OperatorMapper.INSTANCE, name);
        return Optional.ofNullable(operator);
    }

    public List<Operator> search(OperatorSearchFilter filters) {
        Preconditions.checkArgument(Objects.nonNull(filters), "Invalid parameter `filters`: found null object");
        Preconditions.checkArgument(Objects.nonNull(filters.getPageNum()) && filters.getPageNum() > 0, "Invalid page num: %d", filters.getPageNum());
        Preconditions.checkArgument(Objects.nonNull(filters.getPageSize()) && filters.getPageSize() > 0, "Invalid page size: %d", filters.getPageSize());
        boolean hasKeywordFilter = StringUtils.isNotEmpty(filters.getKeyword());
        Integer offset = (filters.getPageNum() - 1) * filters.getPageSize();
        String baseSql = String.format("SELECT id, name, description, params, class_name, package FROM %s ", DB_TABLE_NAME);
        List<Operator> results;
        if (hasKeywordFilter) {
            String sql = baseSql + "WHERE name LIKE CONCAT('%', ?, '%') LIMIT ?, ?";
            results = dbOperator.fetchAll(sql, OperatorMapper.INSTANCE, filters.getKeyword(), offset, filters.getPageSize());
        } else {
            String sql = baseSql + "LIMIT ?, ?";
            results = dbOperator.fetchAll(sql, OperatorMapper.INSTANCE, offset, filters.getPageSize());
        }
        return results;
    }

    /**
     * Create an operator with id assigned
     * @param operator
     */
    public void create(Operator operator) {
        Preconditions.checkArgument(Objects.nonNull(operator), "Invalid parameter `operator`: found null object");
        Preconditions.checkArgument(Objects.nonNull(operator.getId()), "Invalid parameter `operator`: internal `id` not assigned");
        createWithId(operator, operator.getId());
    }

    /**
     * Create an operator with all of its properties but id is assigned individually
     * @param operator Operator object
     * @param id an ID to override the internal value of `operator` object
     */
    public void createWithId(Operator operator, Long id) {
        Preconditions.checkArgument(Objects.nonNull(operator), "Invalid parameter `operator`: found null object");
        String sql = String.format("INSERT INTO %s " +
                "(id, name, description, params, class_name, package) " +
                "VALUES (?, ?, ?, ?, ?, ?);", DB_TABLE_NAME);
        dbOperator.update(
                sql,
                id,
                operator.getName(),
                operator.getDescription(),
                paramsToJsonString(operator.getParams()),
                operator.getClassName(),
                operator.getPackagePath()
        );
    }

    public void updateById(Long id, Operator operator) {
        Preconditions.checkArgument(Objects.nonNull(id), "Invalid parameter `id`: found null object");
        Preconditions.checkArgument(Objects.nonNull(operator), "Invalid parameter `operator`: found null object");
        String sql = String.format("UPDATE %s SET " +
                "name = ?, description = ?, params = ?, class_name = ?, package = ? WHERE id = ?;", DB_TABLE_NAME);
        dbOperator.update(
                sql,
                operator.getName(),
                operator.getDescription(),
                paramsToJsonString(operator.getParams()),
                operator.getClassName(),
                operator.getPackagePath(),
                id
        );
    }

    public void deleteById(Long id) {
        Preconditions.checkArgument(Objects.nonNull(id), "Invalid parameter `id`: found null object");
        String sql = String.format("DELETE FROM %s WHERE id = ?;", DB_TABLE_NAME);
        dbOperator.update(sql, id);
    }

    private static class OperatorMapper implements ResultSetMapper<Operator> {
        public static final OperatorMapper INSTANCE = new OperatorMapper();

        @Override
        public Operator map(ResultSet rs) throws SQLException {
            return Operator.newBuilder()
                    .withId(rs.getLong("id"))
                    .withName(rs.getString("name"))
                    .withParams(jsonStringToParams(rs.getString("params")))
                    .withDescription(rs.getString("description"))
                    .withClassName(rs.getString("class_name"))
                    .withPackagePath(rs.getString("package"))
                    .build();
        }
    }
}
