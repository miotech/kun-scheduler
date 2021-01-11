package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class DatabaseIdentifierProcessorTest {

    @Test
    public void testProcessTableNameIdentifier_athena_startWithNumber() {
        String tableName = "2017-table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, DatabaseType.ATHENA);
        MatcherAssert.assertThat(String.format("\"%s\"", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_athena_startWithNonNumber() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, DatabaseType.ATHENA);
        MatcherAssert.assertThat(String.format("\"%s\"", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_hive() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, DatabaseType.HIVE);
        MatcherAssert.assertThat(String.format("`%s`", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_postgres() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, DatabaseType.POSTGRES);
        MatcherAssert.assertThat(String.format("\"%s\"", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_presto() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, DatabaseType.PRESTO);
        MatcherAssert.assertThat(String.format("`%s`", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessFieldNameIdentifier_athena() {
        String fieldName = "fieldName";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(fieldName, DatabaseType.ATHENA);
        MatcherAssert.assertThat(String.format("\"%s\"", fieldName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessFieldNameIdentifier_hive() {
        String fieldName = "fieldName";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(fieldName, DatabaseType.HIVE);
        MatcherAssert.assertThat(String.format("`%s`", fieldName), Matchers.is(tableNameIdentifier));
    }

}
