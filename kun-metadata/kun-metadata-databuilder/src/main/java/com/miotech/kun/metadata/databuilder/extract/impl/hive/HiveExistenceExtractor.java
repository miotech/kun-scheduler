package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HiveExistenceExtractor implements DatasetExistenceExtractor {

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(hiveDataSource.getMetastoreUrl(), hiveDataSource.getMetastoreUsername(),
                    hiveDataSource.getMetastorePassword(), DatabaseType.MYSQL);
            statement = connection.prepareStatement("SELECT COUNT(1) FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ? AND t.TBL_NAME = ?");
            statement.setString(1, dataset.getDatabaseName());
            statement.setString(2, dataset.getName());

            resultSet = statement.executeQuery();
            long count = 0;
            while (resultSet.next()) {
                count = resultSet.getLong(1);
            }

            return count == 1;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

}
