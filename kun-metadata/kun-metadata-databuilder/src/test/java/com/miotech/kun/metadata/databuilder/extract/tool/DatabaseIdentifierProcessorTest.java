package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class DatabaseIdentifierProcessorTest {

    @Test
    public void testProcessTableNameIdentifier_athena_startWithNumber() {
        String tableName = "2017-table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, ConnectionType.ATHENA);
        MatcherAssert.assertThat(String.format("\"%s\"", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_athena_startWithNonNumber() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, ConnectionType.ATHENA);
        MatcherAssert.assertThat(String.format("\"%s\"", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_hive() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, ConnectionType.HIVE_SERVER);
        MatcherAssert.assertThat(String.format("`%s`", tableName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_postgres() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, ConnectionType.POSTGRESQL);
        MatcherAssert.assertThat(String.format("\"%s\"", tableName), Matchers.is(tableNameIdentifier));
    }

//    @Test
//    public void testProcessTableNameIdentifier_presto() {
//        String tableName = "table";
//        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(tableName, DatabaseType.PRESTO);
//        MatcherAssert.assertThat(String.format("`%s`", tableName), Matchers.is(tableNameIdentifier));
//    }

    @Test
    public void testProcessFieldNameIdentifier_athena() {
        String fieldName = "fieldName";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(fieldName, ConnectionType.ATHENA);
        MatcherAssert.assertThat(String.format("\"%s\"", fieldName), Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessFieldNameIdentifier_hive() {
        String fieldName = "fieldName";
        String tableNameIdentifier = DatabaseIdentifierProcessor.escape(fieldName, ConnectionType.HIVE_SERVER);
        MatcherAssert.assertThat(String.format("`%s`", fieldName), Matchers.is(tableNameIdentifier));
    }

}
