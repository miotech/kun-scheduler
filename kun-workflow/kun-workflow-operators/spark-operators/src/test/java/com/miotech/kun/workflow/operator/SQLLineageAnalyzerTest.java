package com.miotech.kun.workflow.operator;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class SQLLineageAnalyzerTest {

    @Test
    public void parseSQL_with_create_fromselect() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "CREATE TABLE test.a  AS SELECT * FROM test.b";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"test.a"}, is(lineage.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_create_fromunion() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "CREATE TABLE test.a  AS SELECT * FROM test.b UNION SELECT * FROM test.c";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.c", "test.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"test.a"}, is(lineage.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_create_fromjoin() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "CREATE TABLE test.a  AS SELECT * FROM test.b s JOIN test.c t ON s.id = t.id";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.c", "test.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"test.a"}, is(lineage.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_create_and_insert() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "CREATE TABLE test.a  AS SELECT * FROM test.b s JOIN test.c t ON s.id = t.id; " +
                "INSERT INTO test.d SELECT * FROM test.e s JOIN test.f t ON s.id = t.id";
        List<Pair<Set<String>, Set<String>>> lineage = analyzer.parseSQL(sql);
        assertThat(2, is(lineage.size()));
        Pair<Set<String>, Set<String>> lineage1 = lineage.get(0);
        assertThat(new String[]{"test.c", "test.b"}, is(lineage1.getLeft().toArray()));
        assertThat(new String[]{"test.a"}, is(lineage1.getRight().toArray()));

        Pair<Set<String>, Set<String>> lineage2 = lineage.get(1);
        assertThat(new String[]{"test.f", "test.e"}, is(lineage2.getLeft().toArray()));
        assertThat(new String[]{"test.d"}, is(lineage2.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_insert_fromselect() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "INSERT INTO test.a SELECT * FROM test.b s JOIN test.c t ON s.id = t.id";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.c", "test.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"test.a"}, is(lineage.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_insert_from_no_databasename() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE", "test_db");
        String sql = "INSERT INTO a SELECT * FROM b s JOIN test.c t ON s.id = t.id";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.c", "test_db.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"test_db.a"}, is(lineage.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_insert_from_case_insensitive() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "INSERT INTO Test.A SELECT * FROM TEST.B s JOIN test.C t ON s.id = t.id";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.c", "test.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"test.a"}, is(lineage.getRight().toArray()));
    }

    @Test
    public void parseSQL_with_insert_from_escaped() {
        SQLLineageAnalyzer analyzer = new SQLLineageAnalyzer("HIVE");
        String sql = "INSERT INTO `Test`.`A` SELECT * FROM `TEST`.B s JOIN test.C t ON s.id = t.id";
        Pair<Set<String>, Set<String>> lineage = analyzer.parseSQL(sql).get(0);
        assertThat(new String[]{"test.c", "TEST.b"}, is(lineage.getLeft().toArray()));
        assertThat(new String[]{"Test.A"}, is(lineage.getRight().toArray()));
    }
}
