package com.miotech.kun.workflow.db;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseOperator {
    private final Logger logger = LoggerFactory.getLogger(DatabaseOperator.class);

    private final DataSource dataSource;
    private final QueryRunner queryRunner;
    private final AtomicReference<Connection> connInTrans;

    @Inject
    public DatabaseOperator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryRunner = new QueryRunner(dataSource);
        this.connInTrans = new AtomicReference<>(null);
    }

    public <T> T query(String query, ResultSetHandler<T> resultHandler, Object... params) throws SQLException {
        Connection conn = connInTrans.get();
        try {
            if (conn == null) {
                return queryRunner.query(query, resultHandler, params);
            } else {
                return queryRunner.query(conn, query, resultHandler, params);
            }
        } catch (SQLException ex) {
            logger.error("Query failed. query={}, params={}", query, params, ex);
            throw ex;
        }
    }

    public int update(String statement, Object... params) throws SQLException {
        Connection conn = connInTrans.get();
        try {
            if (conn == null) {
                return queryRunner.update(statement, params);
            } else {
                return queryRunner.update(conn, statement, params);
            }
        } catch (SQLException ex) {
            logger.error("Update failed. statement={}, params={}", statement, params, ex);
            throw ex;
        }
    }

    public int[] batch(final String statements, final Object[]... params) throws SQLException {
        Connection conn = connInTrans.get();
        try {
            if (conn == null) {
                return queryRunner.batch(statements, params);
            } else {
                return queryRunner.batch(conn, statements, params);
            }
        } catch (final SQLException ex) {
            logger.error("Batch failed. statements={}, params={}", statements, params, ex);
            throw ex;
        }
    }

    public <T> T transaction(TransactionalOperation<T> operation) throws SQLException {
        if (connInTrans.get() != null) {
            throw new UnsupportedOperationException("Nested transaction is not supported!");
        }

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (!connInTrans.compareAndSet(null, conn)) {
                throw new UnsupportedOperationException("Nested transaction is not supported!");
            }
            conn.setAutoCommit(false);
            T res = operation.doInTransaction(this);
            conn.commit();
            return res;
        } catch (SQLException ex) {
            logger.error("Transaction failed.", ex);
            throw ex;
        } finally {
            DbUtils.closeQuietly(conn);
            connInTrans.set(null); // reset transaction flag
        }
    }
}
