package com.miotech.kun.common.util;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.miotech.kun.common.model.SQLMetaInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/7/20
 */
public class SQLUtil {

    public static SQLMetaInfo parseQuerySQL(String querySql, String dbType) {
        SQLMetaInfo metaInfo = new SQLMetaInfo();
        List<SQLStatement> stmts = SQLUtils.parseStatements(querySql, dbType);
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        SQLStatement stmt = stmts.get(0);
        stmt.accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        metaInfo.setTables(tables.keySet().stream().map(x -> x.getName()).collect(Collectors.toList()));

        List<SQLSelectItem> selectList = ((SQLSelectQueryBlock) ((SQLSelectStatement) stmt).getSelect().getQuery()).getSelectList();
        Set<String> columns = selectList.stream().map(sqlSelectItem -> {
            String column = sqlSelectItem.getAlias();
            if (StringUtils.isEmpty(column)) {
                column = ((SQLIdentifierExpr) sqlSelectItem.getExpr()).getName();
            }
            column = column.replaceAll("\"", "");
            column = column.replaceAll("'", "");
            return column;
        }).collect(Collectors.toSet());
        metaInfo.setColumns(columns);

        return metaInfo;
    }
}
