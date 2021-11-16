package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.postgresql.PostgreSQLStatisticsExtractor;

public class DatasetStatisticsExtractorFactory {

    private DatasetStatisticsExtractorFactory() {
    }

    public static DatasetStatisticsExtractor createExtractor(DataSource dataSource, Dataset dataset , Cataloger cataloger) {
        DatasourceType type = dataSource.getDatasourceType();
        Preconditions.checkNotNull(type);
        switch (type) {
            case HIVE:
                return new HiveStatisticsExtractor(dataSource,dataset,cataloger);
            case POSTGRESQL:
                return new PostgreSQLStatisticsExtractor(dataSource,dataset,cataloger);
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

}
