package com.miotech.kun.dataplatform.common.sparksql.service;

import com.google.common.collect.Lists;
import com.miotech.kun.dataplatform.common.sparksql.vo.SparkSQLExecuteRequest;
import com.miotech.kun.dataplatform.common.sparksql.vo.SparkSQLExecuteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

@Service
public class SparkThriftServerClient {
    private final static int MAX_ROWS = 5000;

    @Value("${spark.thrift-server.enabled}")
    private boolean enabled;

    @Value("${spark.thrift-server.url}")
    private String thriftServerUrl;

    @Value("${spark.thrift-server.username}")
    private String thriftServerUsername;

    @Value("${spark.thrift-server.password}")
    private String thriftServerPassword;

    public SparkSQLExecuteResponse execute(SparkSQLExecuteRequest executeRequest) {
        SparkSQLExecuteResponse.Builder builder = SparkSQLExecuteResponse.builder();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            if (!enabled) {
                builder.msg("ThriftServer is not enabled, SQL will not be executed");
                return builder.build();
            }

            connection = createConnection(thriftServerUrl, thriftServerUsername, thriftServerPassword);
            preparedStatement = connection.prepareStatement(executeRequest.getSql());
            // Set maxRows for statement
            setMaxRows(executeRequest.getPageNum(), executeRequest.getPageSize(), preparedStatement, builder);

            // Execute query
            resultSet = preparedStatement.executeQuery();

            List<String> columnNames = parseMetaData(resultSet);
            builder.columnNames(columnNames);

            int ignoreCount = (executeRequest.getPageNum() - 1) * executeRequest.getPageSize();
            int previousCount = ignoreCount;

            List<List<String>> records = Lists.newArrayList();
            while (resultSet.next()) {
                if (ignoreCount > 0) {
                    ignoreCount--;
                    continue;
                }

                List<String> record = Lists.newArrayList();
                for (int i = 1; i < columnNames.size() + 1; i++) {
                    record.add(resultSet.getString(i));
                }

                records.add(record);
            }

            builder.records(records);
            builder.total(records.size());
            builder.hasNext(records.size() + previousCount == preparedStatement.getMaxRows());
        } catch (Exception e) {
            builder.errorMsg(e.getMessage());
        } finally {
            closeQuietly(connection, preparedStatement, resultSet);
        }

        return builder.build();
    }

    private Connection createConnection(String thriftServerUrl, String thriftServerUsername, String thriftServerPassword)
            throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        return DriverManager.getConnection(thriftServerUrl, thriftServerUsername, thriftServerPassword);
    }

    private void setMaxRows(int pageNum, int pageSize, PreparedStatement preparedStatement,
                            SparkSQLExecuteResponse.Builder builder) throws SQLException {
        int maxRows = pageNum * pageSize;
        if (maxRows > MAX_ROWS) {
            builder.msg("The rows of requests is more than 5000, only 5000 rows will be displayed.");
        }

        preparedStatement.setMaxRows(maxRows <= MAX_ROWS ? maxRows : MAX_ROWS);
    }

    private List<String> parseMetaData(ResultSet resultSet) throws SQLException {
        List<String> columnNames = Lists.newArrayList();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
            columnNames.add(metaData.getColumnName(i).toLowerCase());
        }

        return columnNames;
    }

    private static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) {
            return;
        }

        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception exception) {
            // quiet
        }
    }

}
