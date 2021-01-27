package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.databuilder.extract.impl.arangodb.ArangoDBStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticSearchStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.glue.GlueStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.mongodb.MongoDBStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.postgresql.PostgreSQLStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public class DatasetStatisticsExtractorFactory {

    private DatasetStatisticsExtractorFactory() {
    }

    public static DatasetStatisticsExtractor createExtractor(DataSource.Type type) {
        Preconditions.checkNotNull(type);
        switch (type) {
            case AWS:
                return new GlueStatisticsExtractor();
            case HIVE:
                return new HiveStatisticsExtractor();
            case ARANGO:
                return new ArangoDBStatisticsExtractor();
            case MONGODB:
                return new MongoDBStatisticsExtractor();
            case POSTGRESQL:
                return new PostgreSQLStatisticsExtractor();
            case ELASTICSEARCH:
                return new ElasticSearchStatisticsExtractor();
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

}
