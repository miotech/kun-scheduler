package com.miotech.kun.workflow.operator.resolver;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class SqlResolver{

    private static Map<LineageDirection,Set<String>> streamMap;

    static {
        Set<String> upStreamType = Sets.newHashSet("Select");
        Set<String> downStreamType = Sets.newHashSet("Create","Insert");
        streamMap = new HashMap<>();
        streamMap.put(LineageDirection.UP_STREAM,upStreamType);
        streamMap.put(LineageDirection.DOWN_STREAM,downStreamType);
    }

    public List<String> getDownStream(String sql,String dbType){
        return getTables(sql,dbType,LineageDirection.DOWN_STREAM);
    }

    public List<String> getUpStream(String sql,String dbType){
        return getTables(sql,dbType,LineageDirection.UP_STREAM);
    }

    public List<String> getTables(String sql,String dbType,LineageDirection direction){
        List<SQLStatement> stmts = SQLUtils.parseStatements(sql, dbType);
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        SQLStatement stmt = stmts.get(0);
        stmt.accept(statVisitor);
        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        List<String> result = new ArrayList<>();
        for(Map.Entry<TableStat.Name,TableStat> entry : tables.entrySet()){
            Set<String> tableType = streamMap.get(direction);
            if(tableType.contains(entry.getValue().toString())){
                result.add(entry.getKey().toString());
            }
        }
        return result;
    }
}
