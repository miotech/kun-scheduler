package com.miotech.kun.dataquality.web.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.miotech.kun.dataquality.web.model.entity.SQLParseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SQLParser {

    public SQLParseResult parseQuerySQL(String querySql, String dbType) {
        List<SQLStatement> stmts = SQLUtils.parseStatements(querySql, dbType);
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        SQLStatement sqlStatement = stmts.get(0);
        sqlStatement.accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        tables.forEach((name, tableStat) -> {
            if (tableStat.getCreateCount() > 0
                    || tableStat.getDropCount() > 0
                    || tableStat.getAlterCount() > 0
                    || tableStat.getInsertCount() > 0
                    || tableStat.getDeleteCount() > 0
                    || tableStat.getUpdateCount() > 0
                    || tableStat.getMergeCount() > 0
                    || tableStat.getCreateIndexCount() > 0
                    || tableStat.getDropIndexCount() > 0) {
                throw new UnsupportedOperationException("Only select query is supported.");
            }
        });
        List<String> relatedDatasetNames = tables.keySet().stream().map(x -> x.getName()).collect(Collectors.toList());
        SQLSelectStatement selectStatement = (SQLSelectStatement) sqlStatement;
        SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) selectStatement.getSelect().getQuery();
        List<String> columnNames = queryBlock.getSelectList().stream().map(si -> {
            String alias = si.getAlias();
            if (StringUtils.isNotBlank(alias)) {
                return alias;
            }

            SQLIdentifierExpr expr = (SQLIdentifierExpr) si.getExpr();
            return expr.getName();
        }).collect(Collectors.toList());
        return new SQLParseResult(relatedDatasetNames, columnNames);
    }

}
