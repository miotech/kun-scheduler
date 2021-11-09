package com.miotech.kun.workflow.operator.utils;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ExprUtilsTest {

    @Test
    public void evalExpr() {
        Map<String, Object> args = new HashMap<>();
        ExecuteTarget target = ExecuteTarget.newBuilder()
                .withName("test")
                .withProperties(ImmutableMap.<String, Object>builder()
                        .put("schema", "dev")
                        .build()
                )
                .build();

        String exprRaw = "hello world";
        assertTrue("hello world".equals(ExprUtils.evalExpr(exprRaw, args)));

        args.put("execute_time", "202110271018");
        String exprExecuteTime = "hello world ${execute_time}";
        assertTrue("hello world 202110271018".equals(ExprUtils.evalExpr(exprExecuteTime, args)));


        String exprEnv = "hello world ${ref('testdb.test_table')} ${ref('testdb2.test_table2')}";
        assertTrue("hello world testdb.test_table testdb2.test_table2".equals(ExprUtils.evalExpr(exprEnv, args)));

        args.put("target", target);
        assertTrue("hello world testdb_dev.test_table testdb2_dev.test_table2".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ref('testdb.test_table', 'execute_time')}";
        assertTrue("hello world testdb_dev.test_table_execute_time".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ref('testdb.test_table', execute_time)}";
        assertTrue("hello world testdb_dev.test_table_202110271018".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ ref ( 'testdb.test_table' ) }_${ execute_time }";
        assertTrue("hello world testdb_dev.test_table_202110271018".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ref('testdb.test_table')}_${ target.schema }";
        assertTrue("hello world testdb_dev.test_table_dev".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ref('testdb.test_table')}${suffix('execute_time')}";
        assertTrue("hello world testdb_dev.test_table_execute_time".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ref('testdb.test_table')}${suffix('')}";
        assertTrue("hello world testdb_dev.test_table".equals(ExprUtils.evalExpr(exprEnv, args)));

        exprEnv = "hello world ${ref('testdb.test_table')}${suffix(target.name)}";
        assertTrue("hello world testdb_dev.test_table_test".equals(ExprUtils.evalExpr(exprEnv, args)));
    }

    @Test
    public void evalExecuteTimeExpr() {
        String expr = "hello world ${execute_time}, ${execute_time}";
        String tick = "202110271018";
        assertTrue("hello world 202110271018, 202110271018".equals(ExprUtils.evalExecuteTimeExpr(expr, tick)));
    }
}