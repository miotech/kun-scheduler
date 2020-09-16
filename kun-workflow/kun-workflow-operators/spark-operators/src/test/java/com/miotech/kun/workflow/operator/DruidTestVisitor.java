package com.miotech.kun.workflow.operator;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.dialect.hive.stmt.HiveCreateTableStatement;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public  class DruidTestVisitor extends HiveSchemaStatVisitor {

    private Set<String> udfSet;

    DruidTestVisitor(){
        init();
    }

    private void init(){
        udfSet = new HashSet<>();
        udfSet.add("test_udf");
    }

    @Override
    public boolean visit(SQLMethodInvokeExpr x) {
        this.functions.add(x);
        if(udfSet.contains(x.getMethodName())){
            resolveUDF(x.getArguments());
        }
        return false;
    }

    private void resolveUDF(List<SQLExpr> params){
        TableStat stat = new TableStat();
        stat.incrementCreateCount();
        tableStats.put(new TableStat.Name(params.get(0).toString()),stat);
    }

}
