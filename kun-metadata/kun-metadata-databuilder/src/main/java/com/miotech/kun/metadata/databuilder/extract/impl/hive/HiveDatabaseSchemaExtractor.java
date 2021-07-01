package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

public class HiveDatabaseSchemaExtractor implements Extractor {
    private final HiveDataSource hiveDataSource;

    private final String dbName;

    public HiveDatabaseSchemaExtractor(HiveDataSource hiveDataSource, String dbName) {
        Preconditions.checkNotNull(hiveDataSource, "dataSource should not be null.");
        this.hiveDataSource = hiveDataSource;
        this.dbName = dbName;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> tablesOnMySQL = extractTablesOnMySQL(dbName);
        return Iterators.concat(tablesOnMySQL.stream().map(tableName -> new HiveTableSchemaExtractor(hiveDataSource, dbName, tableName).extract()).iterator());
    }

    private List<String> extractTablesOnMySQL(String dbName) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JDBCClient.getConnection(hiveDataSource.getMetastoreUrl(), hiveDataSource.getMetastoreUsername(),
                    hiveDataSource.getMetastorePassword(), DatabaseType.MYSQL);
            String showTables = "SELECT t.TBL_NAME FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ? AND d.CTLG_NAME = 'hive'";

            statement = connection.prepareStatement(showTables);
            statement.setString(1, dbName);

            resultSet = statement.executeQuery();
            List<String> tables = Lists.newArrayList();
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }

            return tables;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

}
