package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class HiveSchemaExtractor extends HiveExistenceExtractor implements DatasetSchemaExtractor {

    @Override
    public List<DatasetField> extract(Dataset dataset, DataSource dataSource) {
        HiveTableSchemaExtractor hiveTableSchemaExtractor = null;
        try {
            HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
            hiveTableSchemaExtractor = new HiveTableSchemaExtractor(hiveDataSource, dataset.getDatabaseName(), dataset.getName());
            return hiveTableSchemaExtractor.getSchema();
        } finally {
            if (hiveTableSchemaExtractor != null) {
                hiveTableSchemaExtractor.close();
            }
        }
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JDBCClient.getConnection(hiveDataSource.getMetastoreUrl(), hiveDataSource.getMetastoreUsername(),
                    hiveDataSource.getMetastorePassword(), DatabaseType.MYSQL);

            String showDatabases = "SELECT d.NAME FROM DBS d WHERE CTLG_NAME = 'hive'";
            statement = connection.prepareStatement(showDatabases);

            List<String> databases = Lists.newArrayList();
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String database = resultSet.getString(1);
                databases.add(database);
            }

            return Iterators.concat(databases.stream().map(databasesName -> new HiveDatabaseSchemaExtractor(hiveDataSource, databasesName).extract()).iterator());
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }
}
