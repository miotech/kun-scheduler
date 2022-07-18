package com.miotech.kun.workflow.worker.datasource;

import com.miotech.kun.workflow.worker.rpc.ExecutorRpcClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Calendar;


/**
 * rpc proxy preparedStatement
 * real preparedStatement is located in remote rpc server
 */
public class RpcPreparedStatement implements PreparedStatement {

    private final Logger logger = LoggerFactory.getLogger(RpcPreparedStatement.class);
    private final String sql;
    private final ExecutorRpcClient executorRpcClient;
    private final String[] params;
    private final RpcConnectionConfig connectionConfig;


    private String replaceParams(String sql) {
        String executeSql = sql;
        for (String param : params) {
            executeSql = executeSql.replaceFirst("\\?", param);
        }
        return executeSql;
    }

    public RpcPreparedStatement(String sql, ExecutorRpcClient executorRpcClient, RpcConnectionConfig connectionConfig) {
        this.sql = sql;
        this.executorRpcClient = executorRpcClient;
        this.connectionConfig = connectionConfig;
        int parameterCounts = StringUtils.countMatches(sql, '?');
        logger.debug("params count = {}", parameterCounts);
        params = new String[parameterCounts];
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        String executeSql = replaceParams(sql);
        logger.debug("execute query sql : {}", executeSql);
        String rsId = executorRpcClient.executeQuery(connectionConfig, executeSql);
        ResultSet resultSet = new RpcResultSet(executorRpcClient, rsId, connectionConfig);
        return resultSet;
    }

    @Override
    public int executeUpdate() throws SQLException {
        String executeSql = replaceParams(sql);
        logger.debug("execute update sql : {}", executeSql);
        return executorRpcClient.executeUpdate(connectionConfig, executeSql);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setString(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setString(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setString(parameterIndex, x.toString());
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        params[parameterIndex - 1] = x;
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setString(parameterIndex, new String(x));
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setString(parameterIndex, x.toString());
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setString(parameterIndex, x.toString());
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setString(parameterIndex, x.toString());
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void clearParameters() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        if (x instanceof String) {
            setString(parameterIndex, addQuotes((String) x));
        } else if(x instanceof OffsetDateTime){
            setString(parameterIndex, addQuotes(x.toString()));
        } else {
            setString(parameterIndex, x.toString());
        }
    }

    @Override
    public boolean execute() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return new RpcParameterMetaData(params.length);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void close() throws SQLException {
        //do nothing ,preparedStatement will close when resultSet close
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getMaxRows() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        //do nothing
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getResultSetType() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("operation not support for RpcPrepareStatement");
    }

    private String addQuotes(String x) {
        return "'" + x + "'";
    }
}
