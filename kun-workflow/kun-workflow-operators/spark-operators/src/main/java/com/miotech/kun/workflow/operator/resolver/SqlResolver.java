package com.miotech.kun.workflow.operator.resolver;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SqlResolver{

    private Logger logger = LoggerFactory.getLogger(SqlResolver.class);
    private static Map<String,Set<String>> streamMap;

    static {
        Set<String> downStreamType = Sets.newHashSet("Create","Insert");
        Set<String> upStreamType = Sets.newHashSet("Select");
        streamMap = new HashMap<>();
        streamMap.put("downStream",downStreamType);
        streamMap.put("upStream",upStreamType);
    }

    private static final String DOWN_STREAM = "downStream";
    private static final String UP_STREAM = "upStream";

    public List<String> getDownStream(String sql,String dbType){
        return getTables(sql,dbType,DOWN_STREAM);
    }

    public List<String> getUpStream(String sql,String dbType){
        return getTables(sql,dbType,UP_STREAM);
    }

    private List<String> getTables(String sql,String dbType,String direction){
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
