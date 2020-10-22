package com.miotech.kun.workflow.operator.resolver;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.collect.Sets;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.Resolver;

import java.util.*;

public abstract class SqlResolver implements Resolver {

    private static Map<LineageDirection, Set<String>> streamMap;

    static {
        Set<String> upStreamType = Sets.newHashSet("Select");
        Set<String> downStreamType = Sets.newHashSet("Create", "Insert");
        streamMap = new HashMap<>();
        streamMap.put(LineageDirection.UP_STREAM, upStreamType);
        streamMap.put(LineageDirection.DOWN_STREAM, downStreamType);
    }

    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        SqlInfo sqlInfo = getSqlInfoFromConfig(config);
        List<String> tables = getUpStream(sqlInfo.sql, sqlInfo.dbType);
        return toDataStore(config, tables);
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        SqlInfo sqlInfo = getSqlInfoFromConfig(config);
        List<String> tables = getDownStream(sqlInfo.sql, sqlInfo.dbType);
        return toDataStore(config, tables);
    }

    protected abstract SqlInfo getSqlInfoFromConfig(Config config);

    protected abstract List<DataStore> toDataStore(Config config, List<String> tables);

    private List<String> getDownStream(String sql, String dbType) {
        return getTables(sql, dbType, LineageDirection.DOWN_STREAM);
    }

    private List<String> getUpStream(String sql, String dbType) {
        return getTables(sql, dbType, LineageDirection.UP_STREAM);
    }

    private List<String> getTables(String sql, String dbType, LineageDirection direction) {
        List<SQLStatement> stmts = SQLUtils.parseStatements(sql, dbType);
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        SQLStatement stmt = stmts.get(0);
        stmt.accept(statVisitor);
        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        List<String> result = new ArrayList<>();
        for (Map.Entry<TableStat.Name, TableStat> entry : tables.entrySet()) {
            Set<String> tableType = streamMap.get(direction);
            if (tableType.contains(entry.getValue().toString())) {
                result.add(parseTableName(entry.getKey().toString()));
            }
        }
        return result;
    }

    private String parseTableName(String tableName) {
        if (tableName.contains("`")) {
            tableName = tableName.replaceAll("`", "");
        }
        if (tableName.contains("\"")) {
            tableName = tableName.replaceAll("\"", "");
        }
        return tableName;
    }

    class SqlInfo {
        private final String sql;
        private final String dbType;

        SqlInfo(String sql, String dbType) {
            this.sql = sql;
            this.dbType = dbType;
        }
    }
}
