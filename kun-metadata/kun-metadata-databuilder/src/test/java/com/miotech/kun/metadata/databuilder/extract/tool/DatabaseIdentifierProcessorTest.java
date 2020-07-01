package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.tool.DatabaseIdentifierProcessor;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class DatabaseIdentifierProcessorTest {

    @Test
    public void testProcessTableNameIdentifier_athena_startWithNumber() {
        String tableName = "2017-table";
        String target = "\"2017-table\"";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processTableNameIdentifier(tableName, DatabaseType.ATHENA);
        MatcherAssert.assertThat(target, Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_athena_startWithNonNumber() {
        String tableName = "table";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processTableNameIdentifier(tableName, DatabaseType.ATHENA);
        MatcherAssert.assertThat(tableName, Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_hive() {
        String tableName = "table";
        String target = "`table`";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processTableNameIdentifier(tableName, DatabaseType.HIVE);
        MatcherAssert.assertThat(target, Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_postgres() {
        String tableName = "table";
        String target = "`table`";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processTableNameIdentifier(tableName, DatabaseType.POSTGRES);
        MatcherAssert.assertThat(target, Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessTableNameIdentifier_presto() {
        String tableName = "table";
        String target = "`table`";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processTableNameIdentifier(tableName, DatabaseType.PRESTO);
        MatcherAssert.assertThat(target, Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessFieldNameIdentifier_athena() {
        String fieldName = "fieldName";
        String target = "\"fieldName\"";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processFieldNameIdentifier(fieldName, DatabaseType.ATHENA);
        MatcherAssert.assertThat(target, Matchers.is(tableNameIdentifier));
    }

    @Test
    public void testProcessFieldNameIdentifier_hive() {
        String fieldName = "fieldName";
        String target = "`fieldName`";
        String tableNameIdentifier = DatabaseIdentifierProcessor.processFieldNameIdentifier(fieldName, DatabaseType.HIVE);
        MatcherAssert.assertThat(target, Matchers.is(tableNameIdentifier));
    }

}
