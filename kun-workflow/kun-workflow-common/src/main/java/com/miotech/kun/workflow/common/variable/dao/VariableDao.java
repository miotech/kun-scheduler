package com.miotech.kun.workflow.common.variable.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.workflow.core.model.variable.Variable;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class VariableDao {
    protected static final String VARIABLE_MODEL_NAME = "variable";
    protected static final String VARIABLE_TABLE_NAME = "kun_wf_variable";
    private static final List<String> variableCols = ImmutableList.of("key", "value", "is_encrypted");

    @Inject
    private DatabaseOperator dbOperator;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(VARIABLE_MODEL_NAME, variableCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(VARIABLE_TABLE_NAME, VARIABLE_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<Variable> fetchByKey(String key) {
        String sql = getSelectSQL(VARIABLE_MODEL_NAME + ".key = ? ");
        Variable var = dbOperator.fetchOne(sql, VariableMapper.INSTANCE, key);
        return Optional.ofNullable(var);
    }

    public List<Variable> fetchAll() {
        String sql = getSelectSQL(null);
        return dbOperator.fetchAll(sql, VariableMapper.INSTANCE);
    }

    /**
     * fetch all variables starting with a prefix.
     *
     * for example:
     *   key1.key2.key3 = value1
     *   key1.key2.key4 = value1
     *   key1.key2 = value1
     *
     * for prefix: key1.key2
     *     return: key1.key2.key3, key1.key2.key4, key1.key2
     * for prefix: key1.key2.key3
     *     return key1.key2.key4
     * for prefix: key1.key3
     *     return nothing
     * @param prefix: Prefix can be separated with `.` and must not end with `.`
     * @return
     */
    public List<Variable> fetchByPrefix(String prefix) {
        Preconditions.checkArgument(StringUtils.isNoneBlank(prefix), "Prefix should not be blank");
        Preconditions.checkArgument(!prefix.endsWith("."), "Prefix should not endsWith `.`");

        String whereClause = VARIABLE_MODEL_NAME + ".key LIKE CONCAT(CAST(? AS TEXT), '.', '%')";
        String sql = getSelectSQL(whereClause);
        return dbOperator.fetchAll(sql, VariableMapper.INSTANCE, prefix);
    }

    public Variable create(Variable var) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(variableCols.toArray(new String[0]))
                .into(VARIABLE_TABLE_NAME)
                .asPrepared()
                .getSQL();
        dbOperator.update(
                sql,
                var.getFullKey(),
                // TODO: add db encryption for value, currently text for debug reason
                var.getValue(),
                var.isEncrypted()
        );
        return var;
    }

    public Variable update(Variable var) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(VARIABLE_TABLE_NAME)
                .set(variableCols.toArray(new String[0]))
                .where("key = ?")
                .asPrepared()
                .getSQL();
        dbOperator.update(
                sql,
                var.getFullKey(),
                // TODO: add db encryption for value, currently text for debug reason
                var.getValue(),
                var.isEncrypted(),
                var.getFullKey()
        );
        return var;
    }

    public static class VariableMapper implements ResultSetMapper<Variable> {

        public static VariableMapper INSTANCE = new VariableMapper();

        @Override
        public Variable map(ResultSet rs) throws SQLException {
            String fullKey = rs.getString(VARIABLE_MODEL_NAME + "_key");
            String[] keys = fullKey.split("\\.", 2);

            return Variable.newBuilder()
                    .withNamespace(keys[0])
                    .withKey(keys[1])
                    .withValue(rs.getString(VARIABLE_MODEL_NAME + "_value"))
                    .withEncrypted(rs.getBoolean(VARIABLE_MODEL_NAME + "_is_encrypted"))
                    .build();
        }
    }
}
