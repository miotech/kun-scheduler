package com.miotech.kun.commons.db;

import com.google.inject.Inject;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DatabaseOperatorTest extends DatabaseTestBase {
    @Inject
    private DatabaseOperator operator;

    @Test
    public void testUpdate() throws SQLException {
        // prepare
        insertUser("user1", 20);

        // process
        String res = operator.fetchOne("select * from kun_users", (rs) -> rs.getString("name"));

        // verify
        assertThat(res, is("user1"));
    }

    @Test
    public void testFetchOne() {
        // prepare
        insertUser("user1", 20);

        // process
        User res = operator.fetchOne("select * from kun_users", userMapper);

        // verify
        assertThat(res.name, is("user1"));
        assertThat(res.age, is(20));
    }

    @Test
    public void testFetchAll() {
        // prepare
        insertUser("user1", 20);
        insertUser("user2", 30);

        // process
        List<User> res = operator.fetchAll("select * from kun_users", userMapper);

        // verify
        assertThat(res, containsInAnyOrder(
                new User("user1", 20),
                new User("user2", 30)
        ));
    }
    
    @Test
    public void testBatch() {
        // prepare
        operator.batch("insert into kun_users (name, age) values (?, ?)", new Object[][]{
                {"user1", 20},
                {"user2", 30},
        });

        // process
        List<User> res = operator.fetchAll("select * from kun_users", userMapper);

        // verify
        assertThat(res, containsInAnyOrder(
                new User("user1", 20),
                new User("user2", 30)
        ));
    }

    @Test
    public void testTransaction_commit() throws SQLException {
        // prepare
        insertUser("user1", 20);
        assertThat(getRowCount("kun_users"), is(1));

        // process
        operator.transaction(() -> {
            operator.update("insert into kun_users (name) values (?)", "jiang.gu");
            assertThat(getRowCount("kun_users"), is(2));
            return null;
        });

        // verify
        assertThat(getRowCount("kun_users"), is(2));
    }
    @Test
    public void testTransaction_rollback() throws SQLException {
        // prepare
        insertUser("user1", 20);
        assertThat(getRowCount("kun_users"), is(1));

        // process
        try {
            operator.transaction(() -> {
                operator.update("insert into kun_users (name) values (?)", "jiang.gu");
                assertThat(getRowCount("kun_users"), is(2));
                throw new IllegalStateException(); // rollback
            });
        } catch (Exception ex) {
            assertThat(ex, instanceOf(IllegalStateException.class));
        }

        // verify
        assertThat(getRowCount("kun_users"), is(1));
    }
    
    private int getRowCount(String tableName) {
        return operator.fetchOne("select count(*) as cnt from " + tableName, (rs) -> rs.getInt("cnt"));
    }

    private void insertUser(String name, int age) {
        operator.update("insert into kun_users (name, age) values (?, ?)", name, age);
    }
    
    private static class User {
        String name;
        int age;
        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return age == user.age &&
                    Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    private static final ResultSetMapper<User> userMapper =
            rs -> new User(rs.getString("name"), rs.getInt("age"));
}