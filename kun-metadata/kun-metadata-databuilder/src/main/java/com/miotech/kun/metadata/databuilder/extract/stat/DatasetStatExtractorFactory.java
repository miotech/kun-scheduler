package com.miotech.kun.metadata.databuilder.extract.stat;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoStatExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticSearchStatExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.glue.GlueStatExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveStatExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.mongo.MongoStatExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.postgres.PostgreSQLStatExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public class DatasetStatExtractorFactory {

    public static DatasetStatExtractor createExtractor(DataSource.Type type) {
        Preconditions.checkNotNull(type);
        switch (type) {
            case AWS:
                return new GlueStatExtractor();
            case HIVE:
                return new HiveStatExtractor();
            case ARANGO:
                return new ArangoStatExtractor();
            case MONGODB:
                return new MongoStatExtractor();
            case POSTGRESQL:
                return new PostgreSQLStatExtractor();
            case ELASTICSEARCH:
                return new ElasticSearchStatExtractor();
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

}
