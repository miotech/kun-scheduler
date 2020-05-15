package com.miotech.kun.workflow.db;

import com.miotech.kun.workflow.utils.ExceptionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class DatabaseOperator {
    private final Logger logger = LoggerFactory.getLogger(DatabaseOperator.class);

    private final DataSource dataSource;
    private final QueryRunner queryRunner;
    private final ThreadLocal<Connection> connInTrans;

    @Inject
    public DatabaseOperator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryRunner = new QueryRunner(dataSource);
        this.connInTrans = new ThreadLocal<>();
    }

    public <T> T query(String query, ResultSetHandler<T> handler, Object... params) {
        Connection conn = connInTrans.get();
        try {
            if (conn == null) {
                return queryRunner.query(query, handler, params);
            } else {
                return queryRunner.query(conn, query, handler, params);
            }
        } catch (Exception ex) {
            logger.error("Query failed. query={}, params={}", query, params, ex);
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    public <T> T fetchOne(String query, ResultSetMapper<T> mapper, Object... params) {
        return query(query, rs -> rs.next() ? mapper.map(rs) : null, params);
    }

    public <T> List<T> fetchAll(String query, ResultSetMapper<T> mapper, Object... params) {
        return query(query, rs -> {
            if (!rs.next()) {
                return Collections.emptyList();
            }
            List<T> results = new ArrayList<>();
            do {
                results.add(mapper.map(rs));
            } while (rs.next());
            return results;
        }, params);
    }

    public int update(String statement, Object... params) {
        Connection conn = connInTrans.get();
        try {
            if (conn == null) {
                return queryRunner.update(statement, params);
            } else {
                return queryRunner.update(conn, statement, params);
            }
        } catch (Exception ex) {
            logger.error("Update failed. statement={}, params={}", statement, params, ex);
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    public int[] batch(final String statements, final Object[]... params) {
        Connection conn = connInTrans.get();
        try {
            if (conn == null) {
                return queryRunner.batch(statements, params);
            } else {
                return queryRunner.batch(conn, statements, params);
            }
        } catch (Exception ex) {
            logger.error("Batch failed. statements={}, params={}", statements, params, ex);
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    public <T> T transaction(TransactionalOperation<T> operation) {
        if (connInTrans.get() != null) {
            throw new UnsupportedOperationException("Nested transaction is not supported yet! Will implement it if needed.");
        }

        try {
            begin();
            T res = operation.doInTransaction();
            commit();
            return res;
        } catch (Exception ex) {
            logger.error("Transaction failed.", ex);
            rollback();
            throw ExceptionUtils.wrapIfChecked(ex);
        } finally {
            close();
        }
    }

    private void begin() {
        if (connInTrans.get() != null) {
            throw new UnsupportedOperationException("Nested transaction is not supported yet! Will implement it if needed.");
        }

        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            connInTrans.set(conn);
        } catch (SQLException ex) {
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    private void commit() {
        Connection conn = connInTrans.get();
        if (conn == null) {
            throw new IllegalStateException("Not in a valid transaction!");
        }
        try {
            conn.commit();
        } catch (SQLException ex) {
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    private void rollback() {
        Connection conn = connInTrans.get();
        if (conn == null) {
            throw new IllegalStateException("Not in a valid transaction!");
        }
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    private void close() {
        Connection conn = connInTrans.get();
        if (conn != null) {
            connInTrans.remove();
            try {
                conn.close();
            } catch (SQLException ex) {
                throw ExceptionUtils.wrapIfChecked(ex);
            }
        }
    }
}
