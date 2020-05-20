package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.factory.HiveDatasetExtractorFactory;
import com.miotech.kun.metadata.extract.iterator.ExtractIterator;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.HiveDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HiveExtractor extends JDBCExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private HiveDatabase hiveDatabase;

    private HiveExtractor() {};

    public HiveExtractor(HiveDatabase hiveDatabase) {
        init(hiveDatabase);
    }

    private void init(HiveDatabase hiveDatabase) {
        //TODO verify the integrity of the data
        this.hiveDatabase = hiveDatabase;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> tables = new ArrayList<>();

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.MYSQL, hiveDatabase.getMetaStoreUrl(), hiveDatabase.getMetaStoreUsername(),
                    hiveDatabase.getMetaStorePassword());
            String sql = "SELECT t.TBL_NAME FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ?";
            statement = connection.prepareStatement(sql);

            statement.setString(1, hiveDatabase.getName());
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String tableName = resultSet.getString(0);
                tables.add(tableName);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(statement, resultSet);
        }

        return new ExtractIterator(tables, new HiveDatasetExtractorFactory(), hiveDatabase);
    }


}
