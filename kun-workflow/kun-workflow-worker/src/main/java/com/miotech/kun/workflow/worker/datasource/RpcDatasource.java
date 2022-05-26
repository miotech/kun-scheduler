package com.miotech.kun.workflow.worker.datasource;

import com.miotech.kun.workflow.worker.rpc.ExecutorRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;


/**
 * rpc proxy datasource
 * real datasource is located in remote rpc server
 */
public class RpcDatasource implements DataSource {

    private final Logger logger = LoggerFactory.getLogger(RpcDatasource.class);

    private final ExecutorRpcClient executorRpcClient;

    public RpcDatasource(ExecutorRpcClient executorRpcClient) {
        this.executorRpcClient = executorRpcClient;
    }

    @Override
    public Connection getConnection() throws SQLException {
        logger.debug("create connection...");
        String connectionId = executorRpcClient.getConnection();
        return new RpcConnection(executorRpcClient,connectionId);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
