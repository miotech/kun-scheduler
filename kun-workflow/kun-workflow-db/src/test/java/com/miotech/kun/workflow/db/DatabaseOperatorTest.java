package com.miotech.kun.workflow.db;

import com.google.inject.Inject;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class DatabaseOperatorTest extends DatabaseTestBase {
    @Inject
    DatabaseOperator operator;

    @Test
    public void query() throws SQLException {
    }

    @Test
    public void update() throws SQLException {
        operator.update("CREATE TABLE t1 (id bigint serial primary key, name varchar(256) unique);");
        int res = operator.update("insert into t1 (name) values (?)", "ye.ding");
        assertThat(res, is(1));
        String name = operator.query("select * from t1", (rs) -> {
            rs.next(); return rs.getString("name");
        });
        assertThat(name, is("ye.ding"));
    }

    @Test
    public void batch() {
    }

    @Test
    public void transaction() throws SQLException {
        operator.update("CREATE TABLE t1 (id bigint serial primary key, name varchar(256) unique);");

        int res = operator.update("insert into t1 (name) values (?)", "ye.ding");
        assertThat(res, is(1));

        int cnt = operator.query("select count(*) as cnt from t1", (rs) -> {
            rs.next(); return rs.getInt("cnt");
        });
        assertThat(cnt, is(1));

        try {
            operator.transaction(operator -> {
                operator.update("insert into t1 (name) values (?)", "jiang.gu");
                int c = operator.query("select count(*) as cnt from t1", (rs) -> {
                    rs.next(); return rs.getInt("cnt");
                });
                assertThat(c, is(2));
                throw new IllegalStateException(); // rollback
            });
        } catch (Exception ex) {
            assertThat(ex, instanceOf(IllegalStateException.class));
        }

        cnt = operator.query("select count(*) as cnt from t1", (rs) -> {
            rs.next(); return rs.getInt("cnt");
        });
        assertThat(cnt, is(1));
    }
}