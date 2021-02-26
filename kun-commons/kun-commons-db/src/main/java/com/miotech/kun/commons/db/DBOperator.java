package com.miotech.kun.commons.db;

import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBOperator {

    private final DataSource dataSource;
    private final QueryRunner queryRunner;
    private final ThreadLocal<Connection> connInTrans;

    public DBOperator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryRunner = new QueryRunner(dataSource);
        this.connInTrans = new ThreadLocal<>();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public int update(String statement, Object... params) {
        Connection conn = connInTrans.get();
        if (conn == null) {
            throw new IllegalStateException("Not in a valid transaction!");
        }
        try {
            return queryRunner.update(conn, statement, params);
        } catch (Exception ex) {
            throw ExceptionUtils.wrapIfChecked(ex);
        }
    }

    public void begin() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            connInTrans.set(conn);
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        }
    }

    public void commit() {
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

    public void rollback() {
        Connection conn = connInTrans.get();
        if (conn == null) {
            throw new IllegalStateException("Not in a valid transaction!");
        }
        try {
            conn.rollback();
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        }
    }

    public void close() {
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
