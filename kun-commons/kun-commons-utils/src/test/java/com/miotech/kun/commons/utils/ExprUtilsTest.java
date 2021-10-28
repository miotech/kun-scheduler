package com.miotech.kun.commons.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ExprUtilsTest {

    @Test
    public void evalExpr() {
        Map<String, Object> args = new HashMap<>();

        String exprRaw = "hello world";
        assertTrue("hello world".equals(ExprUtils.evalExpr(exprRaw, args)));

        args.put("execute_time", "202110271018");
        String exprExecuteTime = "hello world ${execute_time}";
        assertTrue("hello world 202110271018".equals(ExprUtils.evalExpr(exprExecuteTime, args)));


        String exprEnv = "hello world ${ref('testdb.test_table')} ${ref('testdb2.test_table2')}";
        assertTrue("hello world testdb.test_table testdb2.test_table2".equals(ExprUtils.evalExpr(exprEnv, args)));
        args.put("env", "prod");
        assertTrue("hello world testdb.test_table testdb2.test_table2".equals(ExprUtils.evalExpr(exprEnv, args)));
        args.put("env", "dev");
        assertTrue("hello world testdb_dev.test_table testdb2_dev.test_table2".equals(ExprUtils.evalExpr(exprEnv, args)));

        String exprEnvExecuteTime = "hello world ${ref('testdb.test_table')}_${execute_time}";
        assertTrue("hello world testdb_dev.test_table_202110271018".equals(ExprUtils.evalExpr(exprEnvExecuteTime, args)));

        String exprEnvExecuteTimeSpace = "hello world ${ ref ( 'testdb.test_table' ) }_${ execute_time }";
        assertTrue("hello world testdb_dev.test_table_202110271018".equals(ExprUtils.evalExpr(exprEnvExecuteTimeSpace, args)));
    }

    @Test
    public void evalExecuteTimeExpr() {
        String expr = "hello world ${execute_time}, ${execute_time}";
        String tick = "202110271018";
        assertTrue("hello world 202110271018, 202110271018".equals(ExprUtils.evalExecuteTimeExpr(expr, tick)));
    }

}