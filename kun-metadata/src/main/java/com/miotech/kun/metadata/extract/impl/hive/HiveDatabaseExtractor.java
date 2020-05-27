package com.miotech.kun.metadata.extract.impl.hive;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.HiveCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HiveDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveDatabaseExtractor.class);

    private final HiveCluster cluster;
    private final String database;

    public HiveDatabaseExtractor(HiveCluster cluster, String database) {
        this.cluster = cluster;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> tables = new ArrayList<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.MYSQL, cluster.getMetaStoreUrl(),
                    cluster.getMetaStoreUsername(), cluster.getMetaStorePassword());
            String scanDatabase = "SELECT t.TBL_NAME FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ? AND d.CTLG_NAME = 'hive'";
            statement = connection.prepareStatement(scanDatabase);

            statement.setString(1, database);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                tables.add(tableName);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return Iterators.concat(tables.stream().map((tableName) -> new HiveTableExtractor(cluster, database, tableName).extract()).iterator());
    }

}
