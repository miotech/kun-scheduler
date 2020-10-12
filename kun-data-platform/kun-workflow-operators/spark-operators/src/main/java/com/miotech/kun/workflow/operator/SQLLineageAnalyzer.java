package com.miotech.kun.workflow.operator;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SQLLineageAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(SQLLineageAnalyzer.class);
    private static final String DEFAULT_DATABASE_NAME = "default";
    private static final String TABLE_NAME_SEPARATOR = ".";

    private final String dbType;
    private final String defaultDatabase;

    public SQLLineageAnalyzer(String dbType) {
        this(dbType, null);
    }

    public SQLLineageAnalyzer(String dbType, String defaultDatabase) {
        this.dbType = dbType;
        this.defaultDatabase = StringUtils.isNoneBlank(defaultDatabase) ? defaultDatabase : DEFAULT_DATABASE_NAME;
    }

    public List<Pair<Set<String>, Set<String>>> parseSQL(String sql) {
        logger.debug("Parse SQL : \"{}\"", sql);
        List<SQLStatement> stmts = SQLUtils.parseStatements(sql, dbType);
        if (stmts == null) {
            return Collections.emptyList();
        }

        return stmts.stream()
                .map(this::parseStatement)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Pair<Set<String>, Set<String>> parseStatement(SQLStatement statement) {
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        statement.accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();

        if (tables == null) return null;
        Set<String> fromTables = new HashSet<>();
        Set<String> toTables = new HashSet<>();

        tables.forEach((name, tableStat) -> {
            if (tableStat.getCreateCount() > 0
                    || tableStat.getInsertCount() > 0) {
                String to = name.getName();
                toTables.add(buildName(to));
            } else if (tableStat.getSelectCount() > 0) {
                String from = name.getName();
                fromTables.add(buildName(from));
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("Parse SQL : \"{}\"\n from : {}\n to: {}", statement,  String.join(",", fromTables),
                    String.join(",", toTables));
        }

        return Pair.of(fromTables, toTables);
    }

    private String buildName(String tableName) {
        return buildName(tableName, null);
    }

    /**
     * For HIVE, default lowercase:
     *  `Test`.a -> Test.a
     *  `Test`.A -> Test.a
     *  Test.A -> test.a
     *
     *  For Postgresql, default lowercase:
     *  "Test".a -> Test.a
     *  Test.a -> test.a
     *
     *  For Hive, default uppercase:
     *  "Test".a -> Test.A
     *  Test.a -> TEST.A
     *
     * @param tableName
     * @param dbName
     * @return
     */
    private String buildName(String tableName, String dbName) {
        Preconditions.checkNotNull(tableName, "Table name should not be null");
        String databaseName = StringUtils.isNoneBlank(dbName) ? dbName : defaultDatabase;
        if (tableName.contains(TABLE_NAME_SEPARATOR)) {
            String[] pieces = tableName.split("\\" +TABLE_NAME_SEPARATOR);
            databaseName = pieces[0];
            tableName = pieces[1];
        }
        return parseName(databaseName) + TABLE_NAME_SEPARATOR + parseName(tableName);
    }

    private String parseName(String name) {
        String finalName;
        switch (dbType.toUpperCase()) {
            case "HIVE":
                if (name.contains("`")) {
                    finalName = name.replaceAll("`", "");
                } else {
                    finalName = name.toLowerCase();
                }
                break;
            case "POSTGRESQL":
                if (name.contains("\"")) {
                    finalName = name.replaceAll("\"", "");
                } else {
                    finalName = name.toLowerCase();
                }
                break;
            case "DB2":
            case "ORACLE":
                if (name.contains("\"")) {
                    finalName = name.replaceAll("\"", "");
                } else {
                    finalName = name.toUpperCase();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported SQL dialect: " + dbType.toUpperCase());
        }
        return finalName;
    }
}