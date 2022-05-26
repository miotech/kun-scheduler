package com.miotech.kun.workflow.worker.datasource;

import com.miotech.kun.workflow.worker.rpc.ExecutorRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * rpc proxy connection
 * real connection is located in remote rpc server
 */
public class RpcConnection implements Connection {

    private final Logger logger = LoggerFactory.getLogger(RpcConnection.class);

    private final ExecutorRpcClient executorRpcClient;

    private final String connectionId;

    private boolean autoCommit = true;

    private boolean isClosed = false;

    public RpcConnection(ExecutorRpcClient executorRpcClient, String connectionId) {
        this.executorRpcClient = executorRpcClient;
        this.connectionId = connectionId;
    }

    @Override
    public Statement createStatement() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        logger.debug("create sql {} prepared statement...", sql);
        RpcConnectionConfig connectionConfig = RpcConnectionConfig.newBuilder()
                .withConnectionId(connectionId)
                .withAutoCommit(autoCommit)
                .build();
        return new RpcPreparedStatement(sql, executorRpcClient, connectionConfig);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    @Override
    public void commit() throws SQLException {
        executorRpcClient.commitConnection(connectionId);
    }

    @Override
    public void rollback() throws SQLException {
        executorRpcClient.rollbackConnection(connectionId);
    }

    @Override
    public void close() throws SQLException {
        executorRpcClient.closeConnection(connectionId);
        isClosed = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public String getCatalog() throws SQLException {
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return false;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLException("operation not support for RpcConnection");
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
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
