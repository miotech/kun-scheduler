package com.miotech.kun.workflow.worker.datasource;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.worker.rpc.ExecutorRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class RpcResultSet implements ResultSet {

    private final Logger logger = LoggerFactory.getLogger(RpcResultSet.class);
    private final ExecutorRpcClient executorRpcClient;
    private final String rsId;
    private final RpcConnectionConfig connectionConfig;
    private boolean isClosed = false;

    public RpcResultSet(ExecutorRpcClient executorRpcClient, String rsId, RpcConnectionConfig connectionConfig) {
        this.executorRpcClient = executorRpcClient;
        this.rsId = rsId;
        this.connectionConfig = connectionConfig;
    }

    @Override
    public boolean next() throws SQLException {
        return executorRpcClient.rsNext(rsId);
    }

    @Override
    public void close() throws SQLException {
        executorRpcClient.closeRs(rsId);
        isClosed = true;
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return executorRpcClient.getString(rsId, columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return false;
        }
        return Boolean.valueOf(result);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Byte.valueOf(result);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Short.valueOf(result);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Integer.valueOf(result);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Long.valueOf(result);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Float.valueOf(result);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        String result = getString(columnIndex);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Double.valueOf(result);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        String time = getString(columnIndex);
        return Strings.isNullOrEmpty(time) ? null : Timestamp.valueOf(time);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        logger.debug("get string with label {}", columnLabel);
        return executorRpcClient.getString(rsId, columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return false;
        }
        return Boolean.valueOf(result);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Byte.valueOf(result);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Short.valueOf(result);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Integer.valueOf(result);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Long.valueOf(result);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Float.valueOf(result);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        String result = getString(columnLabel);
        if(Strings.isNullOrEmpty(result)){
            return 0;
        }
        return Double.valueOf(result);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return Date.valueOf(getString(columnLabel));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return Time.valueOf(getString(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        String time = getString(columnLabel);
        return Strings.isNullOrEmpty(time) ? null : Timestamp.valueOf(time);
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return executorRpcClient.getRsMetaData(rsId);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getString(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return getString(columnLabel);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void afterLast() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public boolean first() throws SQLException {
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void insertRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void refreshRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Statement getStatement() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("operation not support for RpcResultSet");
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        String jsonStr = executorRpcClient.getObject(rsId, columnIndex, type.getName());
        logger.debug("get jsonStr = {}", jsonStr);
        return JSONUtils.jsonToObject(jsonStr, type);
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        String jsonStr = executorRpcClient.getObject(rsId, columnLabel, type.getName());
        logger.debug("get jsonStr = {}", jsonStr);
        return JSONUtils.jsonToObject(jsonStr, type);
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
