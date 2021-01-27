package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.statistics.StatisticsExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class HiveStatisticsExtractor extends StatisticsExtractorTemplate {

    @Override
    public DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
        return new DataWarehouseStatTemplate(dataset.getDatabaseName(), null,
                dataset.getName(), DatabaseType.HIVE, hiveDataSource);
    }

    @Override
    public LocalDateTime getLastUpdatedTime(Dataset dataset, DataSource dataSource) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        FileSystem fileSystem = null;

        try {
            connection = JDBCClient.getConnection(hiveDataSource.getMetastoreUrl(), hiveDataSource.getMetastoreUsername(),
                    hiveDataSource.getMetastorePassword(), DatabaseType.MYSQL);
            String locationSQL = "SELECT s.LOCATION FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID JOIN SDS s ON t.SD_ID = s.SD_ID WHERE d.NAME = ? AND t.TBL_NAME = ?";
            statement = connection.prepareStatement(locationSQL);
            statement.setString(1, dataset.getDatabaseName());
            statement.setString(2, dataset.getName());

            resultSet = statement.executeQuery();
            String url = "";
            String path = "";
            while (resultSet.next()) {
                String location = resultSet.getString(1);
                int idx = location.indexOf('/', location.lastIndexOf(':'));

                url = location.substring(0, idx);
                path = location.substring(idx);
            }
            fileSystem = HDFSOperator.create(url + "/" + dataset.getName(), "hdfs");
            FileStatus fileStatus;

            fileStatus = fileSystem.getFileStatus(new Path(path));
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(fileStatus.getModificationTime()), TimeZone.getDefault().toZoneId());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            HDFSOperator.close(fileSystem);
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        HiveExistenceExtractor hiveExistenceExtractor = new HiveExistenceExtractor();
        return hiveExistenceExtractor.judgeExistence(dataset, dataSource, DatasetExistenceJudgeMode.DATASET);
    }
}
