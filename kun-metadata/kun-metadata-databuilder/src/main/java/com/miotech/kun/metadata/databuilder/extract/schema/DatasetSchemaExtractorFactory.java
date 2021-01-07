package com.miotech.kun.metadata.databuilder.extract.schema;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticSearchSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.glue.GlueSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.mongo.MongoSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.postgres.PostgreSQLSchemaExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public class DatasetSchemaExtractorFactory {

    public static DatasetSchemaExtractor createExtractor(DataSource.Type type) {
        Preconditions.checkNotNull(type);
        switch (type) {
            case AWS:
                return new GlueSchemaExtractor();
            case HIVE:
                return new HiveSchemaExtractor();
            case ARANGO:
                return new ArangoSchemaExtractor();
            case MONGODB:
                return new MongoSchemaExtractor();
            case POSTGRESQL:
                return new PostgreSQLSchemaExtractor();
            case ELASTICSEARCH:
                return new ElasticSearchSchemaExtractor();
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

}
