package com.miotech.kun.workflow.db.sql;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class DefaultSQLBuilderTest {

    @Test
    public void test_select() {
        assertEquals("SELECT a, b", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .getSQL());

        assertEquals("SELECT a + b", DefaultSQLBuilder.newBuilder()
                .select("a + b")
                .getSQL());
    }

    @Test
    public void test_insert() {
        assertEquals("INSERT INTO test (a, b) \n", DefaultSQLBuilder.newBuilder()
                .insert("a", "b")
                .into("test")
                .getSQL());
        assertEquals("INSERT INTO test (a, b) \n" +
                "SELECT a, b", DefaultSQLBuilder.newBuilder()
                .insert("a", "b")
                .into("test")
                .select("a", "b")
                .getSQL());
    }

    @Test
    public void test_values() {
        assertEquals("INSERT INTO test (a, b) \n" +
                "VALUES (a, b), (c, d)", DefaultSQLBuilder.newBuilder()
                .insert("a", "b")
                .into("test")
                .values(new String[]{"a", "b"}, new String[]{"c", "d"})
                .getSQL());
        assertEquals("INSERT INTO test (a, b) \n" +
                "VALUES (?, ?)", DefaultSQLBuilder.newBuilder()
                .insert("a", "b")
                .into("test")
                .valueSize(2)
                .getSQL());
    }

    @Test
    public void test_delete() {
        assertEquals("DELETE\n" +
                "FROM test\n" +
                "WHERE a = ?", DefaultSQLBuilder.newBuilder()
                .delete()
                .from("test")
                .where("a = ?")
                .getSQL());
    }


    @Test
    public void test_update() {
        assertEquals("UPDATE test\n" +
                "SET a = ?, b = ?", DefaultSQLBuilder.newBuilder()
                .update("test")
                .set("a", "b")
                .asPrepared()
                .getSQL());
    }

    @Test
    public void test_columns() {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put("a", Collections.singletonList("id"));
        columnsMap.put("b", Collections.singletonList("id"));

        assertEquals("SELECT a.id, b.id", DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .getSQL());
    }

    @Test
    public void autoAliasColumns() {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put("a", Collections.singletonList("id"));
        columnsMap.put("b", Collections.singletonList("id"));

        assertEquals("SELECT a.id AS a_id, b.id AS b_id", DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .autoAliasColumns()
                .getSQL());
    }

    @Test
    public void test_from() {

        assertEquals("SELECT a, b\n" +
                "FROM test", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test")
                .getSQL());

        assertEquals("SELECT a, b\n" +
                "FROM test AS testalias", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test", "testalias")
                .getSQL());
    }

    @Test
    public void test_join() {

        assertEquals("SELECT a, b\n" +
                "FROM test AS testalias\n" +
                "JOIN test2 AS testalias2", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test", "testalias")
                .join("test2", "testalias2")
                .getSQL());

        assertEquals("SELECT a, b\n" +
                "FROM test AS testalias\n" +
                "inner JOIN test2 AS testalias2", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test", "testalias")
                .join("inner", "test2", "testalias2")
                .getSQL());
    }

    @Test
    public void join_shouldHaveAccompaniedOnClauseBeforeNextJoin() {
        // Prepare
        SQLBuilder sqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test", "testalias")
                .join("inner", "test2", "testalias2");

        try {
            // 1. try to insert another join clause without providing ON clause for the first join
            sqlBuilder.join("inner", "test3", "testalias3");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalStateException.class));
        }

        // 2. it should be okay if the first JOIN clause follows an ON clause
        sqlBuilder
                .on("testalias2.tid = test.id")
                .join("inner", "test3", "testalias3");
        String sql = sqlBuilder.getSQL();
        assertEquals(
                "SELECT a, b\n" +
                "FROM test AS testalias\n" +
                "inner JOIN test2 AS testalias2\n" +
                "ON testalias2.tid = test.id\n" +
                "inner JOIN test3 AS testalias3",
                sql
        );
    }

    @Test
    public void test_on() {
        assertEquals("SELECT a, b\n" +
                "FROM test AS testalias\n" +
                "inner JOIN test2 AS testalias2\n" +
                "ON testalias.id = testalias2.id", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test", "testalias")
                .join("inner", "test2", "testalias2")
                .on("testalias.id = testalias2.id")
                .getSQL());
    }

    @Test
    public void test_where() {
        assertEquals("SELECT a, b\n" +
                "FROM test\n" +
                "WHERE a = b", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test")
                .where("a = b")
                .getSQL());
    }

    @Test
    public void test_limit() {
        assertEquals("SELECT a, b\n" +
                "FROM test\n" +
                "LIMIT 100", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test")
                .limit(100)
                .getSQL());
    }

    @Test
    public void orderBy() {
        assertEquals("SELECT a, b\n" +
                "FROM test\n" +
                "ORDER BY a DESC, b ASC", DefaultSQLBuilder.newBuilder()
                .select("a", "b")
                .from("test")
                .orderBy("a DESC", "b ASC")
                .getSQL());
    }

    @Test
    public void asPrepared() {
    }

    @Test
    public void asPlain() {
    }
}