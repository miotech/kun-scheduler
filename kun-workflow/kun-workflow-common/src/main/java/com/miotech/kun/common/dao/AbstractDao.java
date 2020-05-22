package com.miotech.kun.common.dao;

import com.google.inject.Inject;

import org.apache.commons.dbutils.ResultSetHandler;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.utils.ClassUtils;
import com.miotech.kun.workflow.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDao<T> {

    private final String ID_COL = "id";
    private final String CREATE_COL = "create_at";
    private final String UPDATE_COL = "update_at";

    @Inject
    private DatabaseOperator dbOperator;


    private Map<String, InternalTableSchema> cachedSchemaMapping;

    public AbstractDao() {
        cachedSchemaMapping = scanTableSchema();
    }

    public abstract String getTableName();

    public abstract Map<String, String> getTableColumnMapping();

    public abstract ResultSetMapper<T> getMapper();

    public Collection<String> getTableColumns() {
        return getTableColumnMapping().values();
    }

    public T findByID(Long taskRunId) {
        String sql = String.format("SELECT %s FROM %s WHERE id = ? ",
                String.join(",", getTableColumns()),
                getTableName());
        return dbOperator.fetchOne(sql, getMapper(), taskRunId);
    }

    public void deleteByID(Long taskRunId) {
        String sql = String.format("DELETE FROM %s WHERE id = ? ",
                getTableName());
        dbOperator.update(sql, taskRunId);
    }

    public T save(T instance) {
        String sql = String.format("INSERT INTO  %s (%s) VALUES (%s) ",
                getTableName(),
                String.join(",", getTableColumns()),
                String.join(",", StringUtils.repeat("?", getTableColumns().size())));
        dbOperator.update(sql, getParams(instance));
        return instance;
    }

    public T update(T instance) {
        String sql = String.format("UPDATE %s SET %s WHERE %s = ?",
                getTableName(),
                getTableColumns().stream()
                        .map(x -> x + " = ? ")
                        .collect(Collectors.joining(", ")),
                ID_COL
        );
        Object id = resolveParam(instance, Collections.singletonList(ID_COL));
        dbOperator.update(sql, getParams(instance), id);
        return instance;
    }

    private Map<String, InternalTableSchema> scanTableSchema() {
        String sql = String.format("SELECT %s FROM %s LIMIT 0 ",
                String.join(",", getTableColumns()),
                getTableName());

        ResultSetHandler<Map<String, InternalTableSchema>> rsh = rs -> {
            ResultSetMetaData metaData = rs.getMetaData();
            Map<String, InternalTableSchema> internalTableSchemas = new HashMap<>();
            for(int i = 0; i < metaData.getColumnCount(); i++) {
                String colName = metaData.getColumnName(i);
                String colType = metaData.getColumnTypeName(i);
                internalTableSchemas.put(colName, new InternalTableSchema(colName, colType));
            }
            return internalTableSchemas;
        };
        return dbOperator.query(sql, rsh);
    }

    private Object[] getParams(T instance) {

        return getTableColumnMapping()
                .keySet()
                .stream()
                .map(x -> resolveParam(instance, Arrays.asList(x.split("\\."))))
                .toArray();
    }

    private Object resolveParam(Object instance, List<String> attributes) {
        if (attributes.isEmpty()) return null;

        Class<?> clz = instance.getClass();
        Method m =  ClassUtils.getGetterMethod(clz, attributes.get(0));
        try {
            Object attrValue = m.invoke(instance);
            if (attributes.size() > 1) {
                return resolveParam(attrValue, attributes.subList(1, attributes.size()));
            } else {
                return attrValue;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot invoke method: " + m.getName(), e);
        }
    }

    private class InternalTableSchema {
        private final String colName;

        private final String colType;

        InternalTableSchema(String colName, String colType) {
            this.colName = colName;
            this.colType = colType;
        }
    }
}
