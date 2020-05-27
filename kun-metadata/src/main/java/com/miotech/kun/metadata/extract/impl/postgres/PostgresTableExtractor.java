package com.miotech.kun.metadata.extract.impl.postgres;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.extract.tool.StringUtil;
import com.miotech.kun.metadata.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostgresTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(PostgresTableExtractor.class);

    private final PostgresCluster cluster;
    private final String database;
    private final String schema;
    private final String table;

    public PostgresTableExtractor(PostgresCluster cluster, String database, String schema, String table) {
        this.cluster = cluster;
        this.database = database;
        this.schema = schema;
        this.table = table;
    }

    @Override
    public List<DatasetField> getSchema() {
        // Get schema information of table
        List<DatasetField> fields = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useDatabase(cluster.getUrl(), database), cluster.getUsername(), cluster.getPassword());
            String sql = "SELECT column_name, udt_name, '' FROM information_schema.columns WHERE table_name = ? AND table_schema = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, table);
            statement.setString(2, schema);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString(1);
                String type = resultSet.getString(2);
                String description = resultSet.getString(3);

                DatasetField field = new DatasetField(name, type, description);
                fields.add(field);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return fields;
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            long distinctCount = 0;
            long nonnullCount = 0;
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useSchema(cluster.getUrl(), database, schema), cluster.getUsername(), cluster.getPassword());
            String sql = "SELECT COUNT(DISTINCT(" + StringUtil.convertUpperCase(datasetField.getName()) + ")) FROM " + StringUtil.convertUpperCase(table);
            if ("json".equals(datasetField.getType())) {
                sql = "SELECT COUNT(DISTINCT(CAST(" + StringUtil.convertUpperCase(datasetField.getName()) + " AS VARCHAR))) FROM " + StringUtil.convertUpperCase(table);
            } else if ("graphid".equals(datasetField.getType())) {
                return new DatasetFieldStat(datasetField.getName(), distinctCount, nonnullCount, null, new Date());
            }
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                distinctCount = resultSet.getLong(1);
            }

            sql = "SELECT COUNT(*) FROM " + StringUtil.convertUpperCase(table) + " WHERE " + StringUtil.convertUpperCase(datasetField.getName()) + " IS NOT NULL";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                nonnullCount = resultSet.getLong(1);
            }

            DatasetFieldStat result = new DatasetFieldStat(datasetField.getName(), distinctCount, nonnullCount, null, new Date());
            return result;
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            logger.error("sqlException, DatabaseType: {}, database: {}, table: {}", DatabaseType.POSTGRES.getName(), database, table, sqlException);
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    @Override
    public DatasetStat getTableStats() {
        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useSchema(cluster.getUrl(), database, schema), cluster.getUsername(), cluster.getPassword());
            String sql = "SELECT COUNT(*) FROM " + StringUtil.convertUpperCase(table);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Long rowCount = resultSet.getLong(1);
                datasetStatBuilder.withRowCount(rowCount);
                datasetStatBuilder.withStatDate(new Date());
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return datasetStatBuilder.build();
    }

    @Override
    protected DataStore getDataStore() {
        return new PostgresDataStore(cluster.getUrl(), database, schema, table);
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.POSTGRES, table);
    }
}
