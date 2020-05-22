package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.DatasetExtractor;
import com.miotech.kun.metadata.extract.DatasetFieldExtractor;
import com.miotech.kun.metadata.extract.DatasetFieldStatExtractor;
import com.miotech.kun.metadata.extract.DatasetStatExtractor;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HiveTableExtractor implements DatasetExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private Database database;
    private String table;

    public HiveTableExtractor(Database database, String table) {
        this.database = database;
        this.table = table;
    }

    @Override
    public Dataset extract() {
        // Get statistics for table
        DatasetStatRequest.Builder statBuilder = DatasetStatRequest.newBuilder();
        statBuilder.withTable(table).withUrl(database.getUrl()).withUsername(database.getUsername())
                .withPassword(database.getPassword());
        DatasetStatExtractor statExtractor = new HiveDatasetStatExtractor(statBuilder.build());
        DatasetStat datasetStat = statExtractor.extract();

        HiveDatabase hiveDatabase = (HiveDatabase) database;
        // Get schema information of table
        DatasetFieldRequest.Builder fieldRequest = DatasetFieldRequest.Builder.builder();
        fieldRequest.setTable(table).setMetaStoreUrl(hiveDatabase.getMetaStoreUrl())
                .setMetaStoreUsername(hiveDatabase.getMetaStoreUsername())
                .setMetaStorePassword(hiveDatabase.getMetaStorePassword());
        DatasetFieldExtractor fieldExtractor = new HiveDatasetFieldExtractor(fieldRequest.build());
        List<DatasetField> datasetFields = fieldExtractor.extract();

        for (DatasetField datasetField : datasetFields) {
            DatasetFieldStatRequest.Builder fieldStatRequest = DatasetFieldStatRequest.Builder.builder();
            fieldStatRequest.setTable(table).setField(datasetField.getName()).setUrl(database.getUrl())
                    .setUsername(database.getUsername())
                    .setPassword(database.getPassword());
            try {
                Connection connection = JDBCClient.getConnection(DatabaseType.HIVE, database.getUrl(), database.getUsername(), database.getPassword());
                DatasetFieldStatExtractor fieldStatExtractor = new HiveDatasetFieldStatExtractor(fieldStatRequest.build(), connection);
                DatasetFieldStat datasetFieldStat = fieldStatExtractor.extract();

                datasetField.setDatasetFieldStat(datasetFieldStat);
            } catch (ClassNotFoundException classNotFoundException) {
                logger.error("driver class not found, DatabaseType: {}", DatabaseType.HIVE.getName(), classNotFoundException);
                throw new RuntimeException(classNotFoundException);
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }

        }

        Dataset.Builder builder = Dataset.Builder.builder();
        String name = DatasetNameGenerator.generateDatasetName(DatabaseType.HIVE, table);
        //TODO build DataStore
        DataStore dataStore = null;
        builder.setName(name).setDatasetStat(datasetStat).setFields(datasetFields).setDataStore(dataStore);
        return builder.build();
    }

}
