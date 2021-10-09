package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.statistics.StatisticsExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.MetaStoreParseUtil;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.time.OffsetDateTime;

public class HiveStatisticsExtractor extends StatisticsExtractorTemplate {

    @Override
    public DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
        return new DataWarehouseStatTemplate(dataset.getDatabaseName(), null,
                dataset.getName(), DatabaseType.HIVE, hiveDataSource);
    }

    @Override
    public OffsetDateTime getLastUpdatedTime(Dataset dataset, DataSource dataSource) {
        String location = MetaStoreParseUtil.parseLocation(MetaStoreParseUtil.Type.META_STORE_CLIENT, dataSource, dataset.getDatabaseName(), dataset.getName());
        int idx = location.indexOf('/', location.lastIndexOf(':'));
        String url = location.substring(0, idx);
        String path = location.substring(idx);
        FileSystem fileSystem = HDFSOperator.create(url + "/" + dataset.getName(), "hdfs");
        try {
            FileStatus fileStatus = fileSystem.getFileStatus(new Path(path));
            return DateTimeUtils.fromTimestamp(fileStatus.getModificationTime());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            HDFSOperator.close(fileSystem);
        }
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        HiveExistenceExtractor hiveExistenceExtractor = new HiveExistenceExtractor();
        return hiveExistenceExtractor.judgeExistence(dataset, dataSource, DatasetExistenceJudgeMode.DATASET);
    }
}
