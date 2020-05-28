package com.miotech.kun.metadata.extract.impl.hive;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.impl.JDBCExtractor;
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

public class HiveExtractor extends JDBCExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private final HiveCluster cluster;

    public HiveExtractor(HiveCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> databases = Lists.newArrayList();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.MYSQL, cluster.getMetaStoreUrl(),
                    cluster.getMetaStoreUsername(), cluster.getMetaStorePassword());
            String scanDatabase = "SELECT d.NAME FROM DBS d WHERE CTLG_NAME = 'hive'";
            statement = connection.prepareStatement(scanDatabase);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                databases.add(databaseName);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return Iterators.concat(databases.stream().map((databasesName) -> new HiveDatabaseExtractor(cluster, databasesName).extract()).iterator());
    }


}
